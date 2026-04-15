package edu.icet.repository.Impl;

import edu.icet.db.DBConnection;
import edu.icet.model.dto.InvoiceDto;
import edu.icet.model.enums.PaymentMethod;
import edu.icet.repository.InvoiceRepository;
import edu.icet.util.CrudUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class InvoiceRepositoryImpl implements InvoiceRepository {

    @Override
    public int createInvoice(InvoiceDto invoice) {
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            return createInvoice(connection, invoice);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    @Override
    public int createInvoice(Connection connection, InvoiceDto invoice) {
        return CrudUtil.executeUpdateWithGeneratedKeys(connection,
                "INSERT INTO invoice (order_id, invoice_no) VALUES (?,?)",
                invoice.getOrderId(),
                invoice.getInvoiceNo()
        );
    }

    @Override
    public Optional<InvoiceDto> findByOrderId(Integer orderId) {
        return CrudUtil.executeQueryForOptional(
                """
                SELECT i.invoice_id, i.order_id, i.invoice_no, i.generated_at,
                       o.subtotal, o.discount_amount, o.tax, o.total, o.payment_method,
                       e.name AS cashier_name, c.name AS customer_name
                FROM invoice i
                JOIN order_header o ON i.order_id = o.order_id
                JOIN `user` u ON o.cashier_id = u.user_id
                LEFT JOIN employee e ON u.employee_id = e.employee_id
                LEFT JOIN customer c ON o.customer_id = c.customer_id
                WHERE i.order_id = ?
                """,
                this::mapRow,
                orderId
        );
    }

    @Override
    public String generateNextInvoiceNo() {
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            return generateNextInvoiceNo(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    @Override
    public String generateNextInvoiceNo(Connection connection) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "INV-" + datePart + "-%";
        int count = CrudUtil.executeQueryForOptional(connection,
                "SELECT COUNT(*) AS cnt FROM invoice WHERE invoice_no LIKE ? FOR UPDATE",
                rs -> rs.getInt("cnt"),
                prefix
        ).orElse(0);
        return String.format("INV-%s-%04d", datePart, count + 1);
    }

    private InvoiceDto mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("generated_at");
        PaymentMethod method = PaymentMethod.valueOf(rs.getString("payment_method"));
        return new InvoiceDto(
                rs.getInt("invoice_id"),
                rs.getInt("order_id"),
                rs.getString("invoice_no"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now(),
                rs.getString("cashier_name"),
                rs.getString("customer_name"),
                rs.getDouble("subtotal"),
                rs.getDouble("discount_amount"),
                rs.getDouble("tax"),
                rs.getDouble("total"),
                method,
                method.name(),
                null
        );
    }
}
