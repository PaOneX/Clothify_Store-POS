package edu.icet.repository;

import edu.icet.model.dto.ReturnDto;
import edu.icet.model.dto.ReturnItemDto;

import java.sql.Connection;
import java.util.List;

public interface ReturnRepository {
    int createReturn(Connection connection, ReturnDto returnDto);
    void addReturnItem(Connection connection, ReturnItemDto item);
    List<ReturnDto> findAll();
    List<ReturnDto> findByOrderId(Integer orderId);
}
