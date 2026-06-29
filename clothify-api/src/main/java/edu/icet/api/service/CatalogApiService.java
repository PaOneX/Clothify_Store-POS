package edu.icet.api.service;

import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.*;
import edu.icet.service.CategoryService;
import edu.icet.service.OnlineOrderService;
import edu.icet.service.ProductService;
import edu.icet.service.ProductVariantService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatalogApiService {

    private final CategoryService categoryService = ServiceFactory.getInstance().getCategoryService();
    private final ProductService productService = ServiceFactory.getInstance().getProductService();
    private final ProductVariantService variantService = ServiceFactory.getInstance().getProductVariantService();
    private final OnlineOrderService onlineOrderService = ServiceFactory.getInstance().getOnlineOrderService();

    public List<CategoryDto> listCategories() {
        return new ArrayList<>(categoryService.getAllCategories());
    }

    public List<ProductCatalogDto> listProducts(Integer categoryId, String search) {
        List<ProductDto> products = categoryId != null || (search != null && !search.isBlank())
                ? new ArrayList<>(productService.searchProducts(search, categoryId, null, null))
                : new ArrayList<>(productService.getAllProducts());
        Map<Integer, ProductCatalogDto> catalog = new LinkedHashMap<>();
        for (ProductVariantDto variant : variantService.getAllActiveVariants()) {
            ProductCatalogDto entry = catalog.computeIfAbsent(variant.getProductId(), id -> {
                ProductCatalogDto dto = new ProductCatalogDto();
                dto.setId(variant.getProductId());
                dto.setProductName(variant.getProductName());
                dto.setDescription(variant.getDescription());
                dto.setImagePath(variant.getImagePath());
                dto.setImageUrl(buildImageUrl(variant.getImagePath()));
                dto.setCategoryId(variant.getCategoryId());
                dto.setCategoryName(variant.getCategoryName());
                return dto;
            });
            if (matchesProduct(entry, products)) {
                entry.getVariants().add(variant);
            }
        }
        List<ProductCatalogDto> result = new ArrayList<>();
        for (ProductCatalogDto dto : catalog.values()) {
            if (!dto.getVariants().isEmpty()) {
                dto.setMinPrice(dto.getVariants().stream().mapToDouble(ProductVariantDto::getPrice).min().orElse(0));
                dto.setMaxPrice(dto.getVariants().stream().mapToDouble(ProductVariantDto::getPrice).max().orElse(0));
                dto.setTotalQty(dto.getVariants().stream().mapToInt(v -> v.getQtyOnHand() != null ? v.getQtyOnHand() : 0).sum());
                result.add(dto);
            }
        }
        return result;
    }

    public ProductCatalogDto getProduct(int productId) {
        return listProducts(null, null).stream()
                .filter(p -> p.getId() != null && p.getId() == productId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public OnlineOrderResponseDto placeOrder(OnlineOrderRequestDto request) {
        return onlineOrderService.placeOnlineOrder(request);
    }

    public OrderDto getOrderStatus(int orderId, String phone) {
        return onlineOrderService.getOnlineOrderStatus(orderId, phone);
    }

    public static String buildImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return "/api/images/products/placeholder.png";
        }
        if (imagePath.startsWith("images/")) {
            return "/api/images/" + imagePath;
        }
        if (imagePath.startsWith("products/")) {
            return "/api/images/" + imagePath;
        }
        return "/api/images/products/placeholder.png";
    }

    private boolean matchesProduct(ProductCatalogDto entry, List<ProductDto> products) {
        return products.stream().anyMatch(p -> p.getId() != null && p.getId().equals(entry.getId()));
    }
}
