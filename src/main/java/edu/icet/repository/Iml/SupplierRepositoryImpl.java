package edu.icet.repository.Iml;

import edu.icet.db.DBConnection;
import edu.icet.model.dto.SupplierDto;
import edu.icet.repository.SupplierRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SupplierRepositoryImpl implements SupplierRepository {
    @Override
    public void addSupplier(SupplierDto supplierDto)throws Exception {
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO supplier VALUES(?, ?, ?, ?, ?)");
        preparedStatement.setObject(1, supplierDto.getId());
        preparedStatement.setObject(2, supplierDto.getName());
        preparedStatement.setObject(3, supplierDto.getContact());
        preparedStatement.setObject(4, supplierDto.getEmail());
        preparedStatement.setObject(5, supplierDto.getAddress());

        preparedStatement.executeUpdate();
    }

    @Override
    public void updateSupplier(SupplierDto supplierDto) {

    }

    @Override
    public void deleteSupplier(Integer id) {

    }

    @Override
    public ResultSet getAllSuppliers() throws Exception{
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM supplier");
        return preparedStatement.executeQuery();
    }

    @Override
    public void searchSupplier(Integer id) {

    }
}
