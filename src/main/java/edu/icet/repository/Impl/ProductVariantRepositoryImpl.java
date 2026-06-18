package edu.icet.repository.Impl;

import edu.icet.exception.InsufficientStockException;
import edu.icet.model.dto.ProductVariantDto;
import edu.icet.repository.ProductVariantRepository;
import edu.icet.util.CrudUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductVariantRepositoryImpl implements ProductVariantRepository {

    private static final String BASE_SELECT = """
            SELECT v.variant_id, v.product_id, v.sku, v.size, v.color, v.barcode,
                   v.price, v.cost_price, v.qty_on_hand, v.active,
                   p.product_name, p.description, p.image_path,
                   p.category_id, p.supplier_id, c.category_name, s.supplier_name
            FROM product_variant v
            JOIN product p ON v.product_id = p.product_id
            LEFT JOIN category c ON p.category_id = c.category_id
            LEFT JOIN supplier s ON p.supplier_id = s.supplier_id
            """;

    @Override
    public int addVariant(ProductVariantDto variant) {
        return CrudUtil.executeUpdateWithGeneratedKeys(
                "INSERT INTO product_variant (product_id, sku, size, color, barcode, price, cost_price, qty_on_hand, active) VALUES (?,?,?,?,?,?,?,?,?)",
                variant.getProductId(),
                variant.getSku(),
                variant.getSize(),
                variant.getColor(),
                variant.getBarcode(),
                variant.getPrice(),
                variant.getCostPrice() != null ? variant.getCostPrice() : 0,
                variant.getQtyOnHand() != null ? variant.getQtyOnHand() : 0,
                variant.getActive() != null && variant.getActive() ? 1 : 1
        );
    }

    @Override
    public void updateVariant(ProductVariantDto variant) {
        CrudUtil.executeUpdate(
                "UPDATE product_variant SET sku=?, size=?, color=?, barcode=?, price=?, cost_price=?, qty_on_hand=?, active=? WHERE variant_id=?",
                variant.getSku(),
                variant.getSize(),
                variant.getColor(),
                variant.getBarcode(),
                variant.getPrice(),
                variant.getCostPrice() != null ? variant.getCostPrice() : 0,
                variant.getQtyOnHand(),
                variant.getActive() != null && variant.getActive() ? 1 : 0,
                variant.getVariantId()
        );
    }

    @Override
    public void deleteVariant(Integer variantId) {
        CrudUtil.executeUpdate("DELETE FROM product_variant WHERE variant_id = ?", variantId);
    }

    @Override
    public List<ProductVariantDto> findByProductId(Integer productId) {
        return CrudUtil.executeQueryForList(
                BASE_SELECT + " WHERE v.product_id = ? ORDER BY v.size, v.color",
                this::mapRow,
                productId
        );
    }

    @Override
    public List<ProductVariantDto> findAllActive() {
        return CrudUtil.executeQueryForList(
                BASE_SELECT + " WHERE v.active = 1 ORDER BY p.product_name, v.size",
                this::mapRow
        );
    }

    @Override
    public List<ProductVariantDto> search(String term, Integer categoryId, Double minPrice, Double maxPrice) {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE v.active = 1");
        List<Object> params = new ArrayList<>();
        if (term != null && !term.isBlank()) {
            sql.append(" AND (p.product_name LIKE ? OR v.sku LIKE ? OR v.barcode LIKE ?)");
            String like = "%" + term.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }
        if (minPrice != null) {
            sql.append(" AND v.price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND v.price <= ?");
            params.add(maxPrice);
        }
        sql.append(" ORDER BY p.product_name, v.size");
        return CrudUtil.executeQueryForList(sql.toString(), this::mapRow, params.toArray());
    }

    @Override
    public Optional<ProductVariantDto> findById(Integer variantId) {
        return CrudUtil.executeQueryForOptional(
                BASE_SELECT + " WHERE v.variant_id = ?",
                this::mapRow,
                variantId
        );
    }

    @Override
    public Optional<ProductVariantDto> findByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return Optional.empty();
        }
        return CrudUtil.executeQueryForOptional(
                BASE_SELECT + " WHERE v.barcode = ? AND v.active = 1",
                this::mapRow,
                barcode.trim()
        );
    }

    @Override
    public void deductStock(Connection connection, Integer variantId, int qty, String displayName) {
        int rows = CrudUtil.executeUpdate(connection,
                "UPDATE product_variant SET qty_on_hand = qty_on_hand - ? WHERE variant_id = ? AND qty_on_hand >= ?",
                qty, variantId, qty);
        if (rows == 0) {
            throw new InsufficientStockException(displayName);
        }
    }

    @Override
    public void adjustStock(Connection connection, Integer variantId, int changeQty, String displayName) {
        if (changeQty < 0) {
            deductStock(connection, variantId, -changeQty, displayName);
        } else {
            CrudUtil.executeUpdate(connection,
                    "UPDATE product_variant SET qty_on_hand = qty_on_hand + ? WHERE variant_id = ?",
                    changeQty, variantId);
        }
    }

    @Override
    public int getQuantity(Integer variantId) {
        return CrudUtil.executeQueryForOptional(
                "SELECT qty_on_hand FROM product_variant WHERE variant_id = ?",
                rs -> rs.getInt("qty_on_hand"),
                variantId
        ).orElse(0);
    }

    @Override
    public void updateBarcode(Integer variantId, String barcode) {
        CrudUtil.executeUpdate("UPDATE product_variant SET barcode = ? WHERE variant_id = ?", barcode, variantId);
    }

    @Override
    public List<ProductVariantDto> findLowStock(int threshold) {
        return CrudUtil.executeQueryForList(
                BASE_SELECT + " WHERE v.active = 1 AND v.qty_on_hand <= ? ORDER BY v.qty_on_hand",
                this::mapRow,
                threshold
        );
    }

    private ProductVariantDto mapRow(ResultSet rs) throws SQLException {
        return new ProductVariantDto(
                rs.getInt("variant_id"),
                rs.getInt("product_id"),
                rs.getString("sku"),
                rs.getString("size"),
                rs.getString("color"),
                rs.getString("barcode"),
                rs.getDouble("price"),
                rs.getDouble("cost_price"),
                rs.getInt("qty_on_hand"),
                rs.getInt("active") == 1,
                rs.getString("product_name"),
                rs.getString("description"),
                rs.getString("image_path"),
                rs.getObject("category_id") != null ? rs.getInt("category_id") : null,
                rs.getObject("supplier_id") != null ? rs.getInt("supplier_id") : null,
                rs.getString("category_name"),
                rs.getString("supplier_name")
        );
    }
}
