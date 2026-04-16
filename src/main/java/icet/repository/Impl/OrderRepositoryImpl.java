package edu.icet.repository.Impl;

import edu.icet.model.dto.OrderDto;
import edu.icet.model.dto.OrderItemDto;
import edu.icet.model.dto.OrderPaymentDto;
import edu.icet.model.enums.OrderSource;
import edu.icet.model.enums.OrderStatus;
import edu.icet.model.enums.PaymentMethod;
import edu.icet.db.DBConnection;
import edu.icet.repository.OrderRepository;
import edu.icet.util.CrudUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepositoryImpl implements OrderRepository {

    private static final String ORDER_SELECT = """
            SELECT o.order_id, o.order_date, o.cashier_id, e.name AS cashier_name,
                   o.customer_id, c.name AS customer_name, c.phone AS customer_phone,
                   o.subtotal, o.discount_amount, o.discount_id, o.tax, o.total,
                   o.payment_method, o.amount_received, o.change_given, o.status, o.order_source
            FROM order_header o
            JOIN `user` u ON o.cashier_id = u.user_id
            LEFT JOIN employee e ON u.employee_id = e.employee_id
            LEFT JOIN customer c ON o.customer_id = c.customer_id
            """;

    @Override
    public int createOrder(OrderDto order) {
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            return createOrder(connection, order);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    @Override
    public int createOrder(Connection connection, OrderDto order) {
        return CrudUtil.executeUpdateWithGeneratedKeys(connection,
                """
                INSERT INTO order_header (cashier_id, customer_id, subtotal, discount_amount, discount_id,
                    tax, total, payment_method, amount_received, change_given, status, order_source)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                order.getCashierId(),
                order.getCustomerId(),
                order.getSubtotal(),
                order.getDiscountAmount() != null ? order.getDiscountAmount() : 0,
                order.getDiscountId(),
                order.getTax(),
                order.getTotal(),
                order.getPaymentMethod() != null ? order.getPaymentMethod().name() : PaymentMethod.CASH.name(),
                order.getAmountReceived(),
                order.getChangeGiven(),
                order.getStatus().name(),
                order.getOrderSource() != null ? order.getOrderSource().name() : "POS"
        );
    }

    @Override
    public void addOrderItem(OrderItemDto item) {
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            addOrderItem(connection, item);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    @Override
    public void addOrderItem(Connection connection, OrderItemDto item) {
        CrudUtil.executeUpdate(connection,
                "INSERT INTO order_item (order_id, variant_id, qty, unit_price, line_total) VALUES (?,?,?,?,?)",
                item.getOrderId(),
                item.getVariantId(),
                item.getQty(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    @Override
    public void addOrderPayment(Connection connection, OrderPaymentDto payment) {
        CrudUtil.executeUpdate(connection,
                "INSERT INTO order_payment (order_id, method, amount) VALUES (?,?,?)",
                payment.getOrderId(),
                payment.getMethod().name(),
                payment.getAmount()
        );
    }

    @Override
    public List<OrderDto> findAll() {
        return CrudUtil.executeQueryForList(
                ORDER_SELECT + " ORDER BY o.order_date DESC",
                this::mapOrderRow
        );
    }

    @Override
    public List<OrderDto> searchOrders(LocalDate from, LocalDate to, Integer orderId, Integer cashierId) {
        StringBuilder sql = new StringBuilder(ORDER_SELECT + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (orderId != null) {
            sql.append(" AND o.order_id = ?");
            params.add(orderId);
        }
        if (cashierId != null) {
            sql.append(" AND o.cashier_id = ?");
            params.add(cashierId);
        }
        if (from != null) {
            sql.append(" AND DATE(o.order_date) >= ?");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND DATE(o.order_date) <= ?");
            params.add(Date.valueOf(to));
        }
        sql.append(" ORDER BY o.order_date DESC");
        return CrudUtil.executeQueryForList(sql.toString(), this::mapOrderRow, params.toArray());
    }

    @Override
    public List<OrderDto> findByCustomerId(Integer customerId) {
        return CrudUtil.executeQueryForList(
                ORDER_SELECT + " WHERE o.customer_id = ? ORDER BY o.order_date DESC",
                this::mapOrderRow,
                customerId
        );
    }

    @Override
    public Optional<OrderDto> findById(Integer orderId) {
        return CrudUtil.executeQueryForOptional(
                ORDER_SELECT + " WHERE o.order_id = ?",
                this::mapOrderRow,
                orderId
        );
    }

    @Override
    public Optional<OrderDto> findByIdAndCustomerPhone(Integer orderId, String phone) {
        if (phone == null || phone.isBlank()) {
            return Optional.empty();
        }
        return CrudUtil.executeQueryForOptional(
                ORDER_SELECT + " WHERE o.order_id = ? AND c.phone = ?",
                this::mapOrderRow,
                orderId,
                phone.trim()
        );
    }

    @Override
    public Optional<OrderDto> findByInvoiceNo(String invoiceNo) {
        return CrudUtil.executeQueryForOptional(
                ORDER_SELECT + " JOIN invoice i ON o.order_id = i.order_id WHERE i.invoice_no = ?",
                this::mapOrderRow,
                invoiceNo
        );
    }

    @Override
    public List<OrderItemDto> findItemsByOrderId(Integer orderId) {
        return CrudUtil.executeQueryForList(
                """
                SELECT oi.order_item_id, oi.order_id, oi.variant_id, v.product_id,
                       p.product_name, v.size, v.color, oi.qty, oi.unit_price, oi.line_total
                FROM order_item oi
                JOIN product_variant v ON oi.variant_id = v.variant_id
                JOIN product p ON v.product_id = p.product_id
                WHERE oi.order_id = ?
                """,
                this::mapItemRow,
                orderId
        );
    }

    @Override
    public List<OrderPaymentDto> findPaymentsByOrderId(Integer orderId) {
        return CrudUtil.executeQueryForList(
                "SELECT payment_id, order_id, method, amount FROM order_payment WHERE order_id = ?",
                rs -> new OrderPaymentDto(
                        rs.getInt("payment_id"),
                        rs.getInt("order_id"),
                        PaymentMethod.valueOf(rs.getString("method")),
                        rs.getDouble("amount")
                ),
                orderId
        );
    }

    private OrderDto mapOrderRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("order_date");
        return new OrderDto(
                rs.getInt("order_id"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now(),
                rs.getInt("cashier_id"),
                rs.getString("cashier_name"),
                rs.getObject("customer_id") != null ? rs.getInt("customer_id") : null,
                rs.getString("customer_name"),
                rs.getDouble("subtotal"),
                rs.getDouble("discount_amount"),
                rs.getObject("discount_id") != null ? rs.getInt("discount_id") : null,
                rs.getDouble("tax"),
                rs.getDouble("total"),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                rs.getObject("amount_received") != null ? rs.getDouble("amount_received") : null,
                rs.getObject("change_given") != null ? rs.getDouble("change_given") : null,
                OrderStatus.valueOf(rs.getString("status")),
                OrderSource.valueOf(rs.getString("order_source")),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    private OrderItemDto mapItemRow(ResultSet rs) throws SQLException {
        return new OrderItemDto(
                rs.getInt("order_item_id"),
                rs.getInt("order_id"),
                rs.getInt("variant_id"),
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("size"),
                rs.getString("color"),
                rs.getInt("qty"),
                rs.getDouble("unit_price"),
                rs.getDouble("line_total")
        );
    }
}
