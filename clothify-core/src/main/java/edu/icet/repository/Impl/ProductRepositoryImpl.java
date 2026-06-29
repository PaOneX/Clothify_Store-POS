package edu.icet.repository.Impl;

import edu.icet.model.dto.ProductDto;
import edu.icet.repository.ProductRepository;
import edu.icet.util.CrudUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryImpl implements ProductRepository {

    private static final String BASE_SELECT = """
            SELECT p.product_id, p.product_name, p.description, p.image_path,
                   p.category_id, p.supplier_id, c.category_name, s.supplier_name,
                   MIN(v.price) AS min_price, MAX(v.price) AS max_price,
                   COALESCE(SUM(v.qty_on_hand), 0) AS total_qty,
                   COUNT(v.variant_id) AS variant_count
            FROM product p
            LEFT JOIN product_variant v ON p.product_id = v.product_id
            LEFT JOIN category c ON p.category_id = c.category_id
            LEFT JOIN supplier s ON p.supplier_id = s.supplier_id
            """;

    @Override
    public int addProduct(ProductDto productDto) {
        return CrudUtil.executeUpdateWithGeneratedKeys(
                "INSERT INTO product (product_name, description, image_path, category_id, supplier_id) VALUES (?,?,?,?,?)",
                productDto.getProductName(),
                productDto.getDescription(),
                productDto.getImagePath(),
                productDto.getCategoryId(),
                productDto.getSupplierId()
        );
    }

    @Override
    public void editProduct(ProductDto productDto) {
        CrudUtil.executeUpdate(
                "UPDATE product SET product_name=?, description=?, image_path=?, category_id=?, supplier_id=? WHERE product_id=?",
                productDto.getProductName(),
                productDto.getDescription(),
                productDto.getImagePath(),
                productDto.getCategoryId(),
                productDto.getSupplierId(),
                productDto.getId()
        );
    }

    @Override
    public void deleteProduct(Integer id) {
        CrudUtil.executeUpdate("DELETE FROM product WHERE product_id = ?", id);
    }

    @Override
    public List<ProductDto> getProducts() {
        return CrudUtil.executeQueryForList(
                BASE_SELECT + " GROUP BY p.product_id ORDER BY p.product_id",
                this::mapRow
        );
    }

    @Override
    public List<ProductDto> searchProducts(String name, Integer categoryId, Double minPrice, Double maxPrice) {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            sql.append(" AND p.product_name LIKE ?");
            params.add("%" + name.trim() + "%");
        }
        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }
        sql.append(" GROUP BY p.product_id");
        if (minPrice != null) {
            sql.append(" HAVING MAX(v.price) >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(minPrice != null ? " AND MIN(v.price) <= ?" : " HAVING MIN(v.price) <= ?");
            params.add(maxPrice);
        }
        sql.append(" ORDER BY p.product_id");
        return CrudUtil.executeQueryForList(sql.toString(), this::mapRow, params.toArray());
    }

    private ProductDto mapRow(ResultSet rs) throws SQLException {
        return new ProductDto(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("description"),
                rs.getString("image_path"),
                rs.getObject("category_id") != null ? rs.getInt("category_id") : null,
                rs.getObject("supplier_id") != null ? rs.getInt("supplier_id") : null,
                rs.getString("category_name"),
                rs.getString("supplier_name"),
                rs.getObject("min_price") != null ? rs.getDouble("min_price") : null,
                rs.getObject("max_price") != null ? rs.getDouble("max_price") : null,
                rs.getInt("total_qty"),
                rs.getInt("variant_count")
        );
    }
}
