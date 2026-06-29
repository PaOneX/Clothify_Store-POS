const API_BASE = '';
const API_KEY = 'clothify-dev-key';

const state = {
    categories: [],
    products: [],
    selectedCategoryId: null,
    cart: [],
    modalProduct: null,
    selectedVariant: null
};

async function api(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (options.method === 'POST' && path.startsWith('/api/orders')) {
        headers['X-API-Key'] = API_KEY;
    }
    const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || 'Request failed');
    }
    return response.json();
}

function formatPrice(value) {
    return `Rs. ${Number(value).toFixed(2)}`;
}

function formatSize(size) {
    const map = { XS: 'XS', S: 'S', M: 'M', L: 'L', XL: 'XL', XXL: 'XXL', FREE: 'One Size' };
    return map[size] || size || '—';
}

async function loadCategories() {
    state.categories = await api('/api/categories');
    const list = document.getElementById('categoryList');
    list.innerHTML = '';
    const allBtn = document.createElement('button');
    allBtn.textContent = 'All Categories';
    allBtn.className = 'category-btn active';
    allBtn.onclick = () => selectCategory(null, allBtn);
    list.appendChild(allBtn);
    state.categories.forEach(category => {
        const btn = document.createElement('button');
        btn.textContent = category.categoryName;
        btn.className = 'category-btn';
        btn.onclick = () => selectCategory(category.id, btn);
        list.appendChild(btn);
    });
}

async function loadProducts() {
    const params = new URLSearchParams();
    if (state.selectedCategoryId) params.set('categoryId', state.selectedCategoryId);
    const search = document.getElementById('searchInput').value.trim();
    if (search) params.set('search', search);
    const query = params.toString();
    state.products = await api(`/api/products${query ? `?${query}` : ''}`);
    renderProducts();
}

function selectCategory(categoryId, button) {
    state.selectedCategoryId = categoryId;
    document.querySelectorAll('.category-btn').forEach(btn => btn.classList.remove('active'));
    button.classList.add('active');
    loadProducts();
}

function renderProducts() {
    const grid = document.getElementById('productGrid');
    grid.innerHTML = '';
    state.products.forEach(product => {
        const card = document.createElement('article');
        card.className = 'product-card';
        const outOfStock = (product.totalQty || 0) <= 0;
        if (outOfStock) card.classList.add('out-of-stock');
        card.innerHTML = `
            <div class="image-wrap">
                ${product.variants?.length > 1 ? '<span class="variation-badge">⧉</span>' : ''}
                <img src="${product.imageUrl || '/api/images/products/placeholder.png'}" alt="${product.productName}">
                ${outOfStock ? '<span class="stock-overlay">Out Of Stock</span>' : ''}
            </div>
            <h3>${product.productName}</h3>
            <p class="price">${product.minPrice === product.maxPrice
                ? formatPrice(product.minPrice)
                : `${formatPrice(product.minPrice)} – ${formatPrice(product.maxPrice)}`}</p>
        `;
        if (!outOfStock) {
            card.onclick = () => openSizeModal(product);
        }
        grid.appendChild(card);
    });
}

function openSizeModal(product) {
    state.modalProduct = product;
    state.selectedVariant = null;
    document.getElementById('modalProductName').textContent = product.productName;
    document.getElementById('modalImage').src = product.imageUrl || '/api/images/products/placeholder.png';
    document.getElementById('modalPrice').textContent = formatPrice(product.minPrice);
    const sizeOptions = document.getElementById('sizeOptions');
    sizeOptions.innerHTML = '';
    const addBtn = document.getElementById('addToCartBtn');
    addBtn.disabled = true;
    product.variants.forEach(variant => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'size-btn';
        btn.textContent = formatSize(variant.size);
        const inStock = (variant.qtyOnHand || 0) > 0;
        if (!inStock) {
            btn.disabled = true;
            btn.textContent += ' (Out)';
        } else {
            btn.onclick = () => {
                state.selectedVariant = variant;
                document.querySelectorAll('.size-btn').forEach(el => el.classList.remove('selected'));
                btn.classList.add('selected');
                document.getElementById('modalPrice').textContent =
                    `${formatPrice(variant.price)} · Stock ${variant.qtyOnHand}`;
                addBtn.disabled = false;
            };
        }
        sizeOptions.appendChild(btn);
    });
    document.getElementById('sizeModal').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('sizeModal').classList.add('hidden');
}

function addSelectedToCart() {
    if (!state.selectedVariant) return;
    const existing = state.cart.find(item => item.variantId === state.selectedVariant.variantId);
    if (existing) {
        existing.qty += 1;
    } else {
        state.cart.push({
            variantId: state.selectedVariant.variantId,
            productName: state.modalProduct.productName,
            size: state.selectedVariant.size,
            price: state.selectedVariant.price,
            qty: 1
        });
    }
    renderCart();
    closeModal();
}

function renderCart() {
    const container = document.getElementById('cartItems');
    container.innerHTML = '';
    let total = 0;
    let count = 0;
    state.cart.forEach((item, index) => {
        total += item.price * item.qty;
        count += item.qty;
        const row = document.createElement('div');
        row.className = 'cart-row';
        row.innerHTML = `
            <div>
                <strong>${item.qty}× ${item.productName}</strong>
                <div class="meta">${formatSize(item.size)} · ${formatPrice(item.price)}</div>
            </div>
            <div class="cart-actions">
                <button type="button" data-action="minus">-</button>
                <button type="button" data-action="plus">+</button>
                <button type="button" data-action="remove">×</button>
            </div>
        `;
        row.querySelector('[data-action="minus"]').onclick = () => {
            item.qty -= 1;
            if (item.qty <= 0) state.cart.splice(index, 1);
            renderCart();
        };
        row.querySelector('[data-action="plus"]').onclick = () => {
            item.qty += 1;
            renderCart();
        };
        row.querySelector('[data-action="remove"]').onclick = () => {
            state.cart.splice(index, 1);
            renderCart();
        };
        container.appendChild(row);
    });
    document.getElementById('cartCount').textContent = String(count);
    document.getElementById('cartTotal').textContent = formatPrice(total);
}

async function submitOrder(event) {
    event.preventDefault();
    const message = document.getElementById('orderMessage');
    message.textContent = '';
    if (state.cart.length === 0) {
        message.textContent = 'Your cart is empty.';
        return;
    }
    try {
        const payload = {
            customerName: document.getElementById('customerName').value.trim(),
            customerPhone: document.getElementById('customerPhone').value.trim(),
            customerEmail: document.getElementById('customerEmail').value.trim() || null,
            customerAddress: document.getElementById('customerAddress').value.trim() || null,
            items: state.cart.map(item => ({ variantId: item.variantId, qty: item.qty }))
        };
        const result = await api('/api/orders', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        state.cart = [];
        renderCart();
        message.textContent = `${result.message} Order #${result.orderId} · Total ${formatPrice(result.total)}`;
        document.getElementById('checkoutForm').reset();
    } catch (error) {
        message.textContent = error.message;
    }
}

document.getElementById('searchBtn').onclick = loadProducts;
document.getElementById('searchInput').addEventListener('keydown', event => {
    if (event.key === 'Enter') loadProducts();
});
document.getElementById('closeModal').onclick = closeModal;
document.getElementById('addToCartBtn').onclick = addSelectedToCart;
document.getElementById('checkoutForm').addEventListener('submit', submitOrder);

loadCategories().then(loadProducts);
