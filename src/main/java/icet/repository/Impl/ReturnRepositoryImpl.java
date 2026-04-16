package edu.icet.repository.Impl;

import edu.icet.model.dto.ReturnDto;
import edu.icet.model.dto.ReturnItemDto;
import edu.icet.model.enums.ReturnType;
import edu.icet.repository.ReturnRepository;
import edu.icet.util.CrudUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReturnRepositoryImpl implements ReturnRepository {

    @Override
    public int createReturn(Connection connection, ReturnDto returnDto) {
        return CrudUtil.executeUpdateWithGeneratedKeys(connection,
                "INSERT INTO return_header (order_id, cashier_id, reason, refund_amount, return_type) VALUES (?,?,?,?,?)",
                returnDto.getOrderId(),
                returnDto.getCashierId(),
                returnDto.getReason(),
                returnDto.getRefundAmount(),
                returnDto.getReturnType().name()
        );
    }

    @Override
    public void addReturnItem(Connection connection, ReturnItemDto item) {
        CrudUtil.executeUpdate(connection,
                "INSERT INTO return_item (return_id, variant_id, qty, unit_price) VALUES (?,?,?,?)",
                item.getReturnId(),
                item.getVariantId(),
                item.getQty(),
                item.getUnitPrice()
        );
    }

    @Override
    public List<ReturnDto> findAll() {
        return CrudUtil.executeQueryForList(
                """
                SELECT r.return_id, r.order_id, r.return_date, r.cashier_id, e.name AS cashier_name,
                       r.reason, r.refund_amount, r.return_type
                FROM return_header r
                JOIN `user` u ON r.cashier_id = u.user_id
                LEFT JOIN employee e ON u.employee_id = e.employee_id
                ORDER BY r.return_date DESC
                """,
                this::mapReturn
        );
    }

    @Override
    public List<ReturnDto> findByOrderId(Integer orderId) {
        return CrudUtil.executeQueryForList(
                """
                SELECT r.return_id, r.order_id, r.return_date, r.cashier_id, e.name AS cashier_name,
                       r.reason, r.refund_amount, r.return_type
                FROM return_header r
                JOIN `user` u ON r.cashier_id = u.user_id
                LEFT JOIN employee e ON u.employee_id = e.employee_id
                WHERE r.order_id = ?
                ORDER BY r.return_date DESC
                """,
                this::mapReturn,
                orderId
        );
    }

    private ReturnDto mapReturn(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("return_date");
        return new ReturnDto(
                rs.getInt("return_id"),
                rs.getInt("order_id"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now(),
                rs.getInt("cashier_id"),
                rs.getString("cashier_name"),
                rs.getString("reason"),
                rs.getDouble("refund_amount"),
                ReturnType.valueOf(rs.getString("return_type")),
                new ArrayList<>()
        );
    }
}
