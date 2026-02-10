package edu.icet.repository.Iml;

import edu.icet.db.DBConnection;
import edu.icet.model.dto.ProductDto;
import edu.icet.repository.ProductRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ProductRepositoryImpl implements ProductRepository {
    @Override
    public void addProduct(ProductDto productDto) {
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO product " +
                    "(product_name, description, price, qty_on_hand, category_id," +
                    " supplier_id) VALUES (?,?,?,?,?,?)");

            preparedStatement.setObject(1, productDto.getProductName());
            preparedStatement.setObject(3, productDto.getPrice());
            preparedStatement.setObject(4, productDto.getQty());
            preparedStatement.setObject(5, productDto.getCategory());
            preparedStatement.setObject(6, productDto.getSupplierId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void editProduct(ProductDto productDto) {

    }

    @Override
    public void deleteProduct(Integer id) throws Exception {

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM product WHERE product_id = ?");
        preparedStatement.setObject(1, id);
        preparedStatement.executeUpdate();

    }

    @Override
    public ResultSet getProducts() throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM product");
        return preparedStatement.executeQuery();
    }
}
