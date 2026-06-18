package edu.icet.repository.Impl;

import edu.icet.model.dto.*;
import edu.icet.repository.ReportRepository;
import edu.icet.util.CrudUtil;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportRepositoryImpl implements ReportRepository {

    @Override
    public SalesSummaryDto getSalesSummary(LocalDate from, LocalDate to) {
        return CrudUtil.executeQueryForOptional(
                """
                SELECT COUNT(*) AS order_count,
                       COALESCE(SUM(total), 0) AS total_revenue,
                       COALESCE(SUM(tax), 0) AS total_tax
                FROM order_header
                WHERE status = 'COMPLETED'
                  AND DATE(order_date) >= ? AND DATE(order_date) <= ?
                """,
                this::mapSalesSummary,
                Date.valueOf(from), Date.valueOf(to)
        ).orElse(new SalesSummaryDto(0, 0, 0));
    }

    @Override
    public InventorySummaryDto getInventorySummary(int lowStockThreshold) {
        return CrudUtil.executeQueryForOptional(
                """
                SELECT COUNT(*) AS total_skus,
                       COALESCE(SUM(qty_on_hand), 0) AS total_units,
                       SUM(CASE WHEN qty_on_hand < ? THEN 1 ELSE 0 END) AS low_stock_count
                FROM product_variant WHERE active = 1
                """,
                rs -> new InventorySummaryDto(
                        rs.getInt("total_skus"),
                        rs.getInt("total_units"),
                        rs.getInt("low_stock_count")
                ),
                lowStockThreshold
        ).orElse(new InventorySummaryDto(0, 0, 0));
    }

    @Override
    public List<TopProductDto> getTopProducts(LocalDate from, LocalDate to, int limit) {
        return CrudUtil.executeQueryForList(
                """
                SELECT v.variant_id AS product_id, CONCAT(p.product_name, ' (', COALESCE(v.size,''), ')') AS product_name,
                       SUM(oi.qty) AS total_qty_sold,
                       SUM(oi.line_total) AS total_revenue
                FROM order_item oi
                JOIN order_header o ON oi.order_id = o.order_id
                JOIN product_variant v ON oi.variant_id = v.variant_id
                JOIN product p ON v.product_id = p.product_id
                WHERE o.status = 'COMPLETED'
                  AND DATE(o.order_date) >= ? AND DATE(o.order_date) <= ?
                GROUP BY v.variant_id, p.product_name, v.size
                ORDER BY total_qty_sold DESC
                LIMIT ?
                """,
                rs -> new TopProductDto(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("total_qty_sold"),
                        rs.getDouble("total_revenue")
                ),
                Date.valueOf(from), Date.valueOf(to), limit
        );
    }

    @Override
    public List<DailySalesDto> getDailySales(LocalDate from, LocalDate to) {
        return CrudUtil.executeQueryForList(
                """
                SELECT DATE(order_date) AS sale_date,
                       COUNT(*) AS order_count,
                       COALESCE(SUM(total), 0) AS revenue
                FROM order_header
                WHERE status = 'COMPLETED'
                  AND DATE(order_date) >= ? AND DATE(order_date) <= ?
                GROUP BY DATE(order_date)
                ORDER BY sale_date
                """,
                rs -> new DailySalesDto(
                        rs.getString("sale_date"),
                        rs.getInt("order_count"),
                        rs.getDouble("revenue")
                ),
                Date.valueOf(from), Date.valueOf(to)
        );
    }

    @Override
    public List<MonthlySalesDto> getMonthlySales(LocalDate from, LocalDate to) {
        return CrudUtil.executeQueryForList(
                """
                SELECT DATE_FORMAT(order_date, '%Y-%m') AS month,
                       COUNT(*) AS order_count,
                       COALESCE(SUM(total), 0) AS revenue
                FROM order_header
                WHERE status = 'COMPLETED'
                  AND DATE(order_date) >= ? AND DATE(order_date) <= ?
                GROUP BY DATE_FORMAT(order_date, '%Y-%m')
                ORDER BY month
                """,
                rs -> new MonthlySalesDto(
                        rs.getString("month"),
                        rs.getInt("order_count"),
                        rs.getDouble("revenue")
                ),
                Date.valueOf(from), Date.valueOf(to)
        );
    }

    @Override
    public ProfitSummaryDto getProfitSummary(LocalDate from, LocalDate to) {
        return CrudUtil.executeQueryForOptional(
                """
                SELECT COALESCE(SUM(oi.line_total), 0) AS revenue,
                       COALESCE(SUM(oi.qty * v.cost_price), 0) AS cost
                FROM order_item oi
                JOIN order_header o ON oi.order_id = o.order_id
                JOIN product_variant v ON oi.variant_id = v.variant_id
                WHERE o.status = 'COMPLETED'
                  AND DATE(o.order_date) >= ? AND DATE(o.order_date) <= ?
                """,
                rs -> {
                    double revenue = rs.getDouble("revenue");
                    double cost = rs.getDouble("cost");
                    return new ProfitSummaryDto(revenue, cost, revenue - cost);
                },
                Date.valueOf(from), Date.valueOf(to)
        ).orElse(new ProfitSummaryDto(0, 0, 0));
    }

    @Override
    public List<CategoryRevenueDto> getRevenueByCategory(LocalDate from, LocalDate to) {
        return CrudUtil.executeQueryForList(
                """
                SELECT COALESCE(c.category_name, 'Uncategorized') AS category_name,
                       COALESCE(SUM(oi.line_total), 0) AS revenue
                FROM order_item oi
                JOIN order_header o ON oi.order_id = o.order_id
                JOIN product_variant v ON oi.variant_id = v.variant_id
                JOIN product p ON v.product_id = p.product_id
                LEFT JOIN category c ON p.category_id = c.category_id
                WHERE o.status = 'COMPLETED'
                  AND DATE(o.order_date) >= ? AND DATE(o.order_date) <= ?
                GROUP BY c.category_name
                ORDER BY revenue DESC
                """,
                rs -> new CategoryRevenueDto(rs.getString("category_name"), rs.getDouble("revenue")),
                Date.valueOf(from), Date.valueOf(to)
        );
    }

    @Override
    public List<SlowMovingDto> getSlowMovingProducts(int days, int limit) {
        return CrudUtil.executeQueryForList(
                """
                SELECT v.variant_id, p.product_name, v.size, v.qty_on_hand,
                       COALESCE(DATEDIFF(CURDATE(), MAX(o.order_date)), 999) AS days_since
                FROM product_variant v
                JOIN product p ON v.product_id = p.product_id
                LEFT JOIN order_item oi ON oi.variant_id = v.variant_id
                LEFT JOIN order_header o ON oi.order_id = o.order_id AND o.status = 'COMPLETED'
                WHERE v.active = 1 AND v.qty_on_hand > 0
                GROUP BY v.variant_id, p.product_name, v.size, v.qty_on_hand
                HAVING days_since >= ?
                ORDER BY days_since DESC
                LIMIT ?
                """,
                rs -> new SlowMovingDto(
                        rs.getInt("variant_id"),
                        rs.getString("product_name"),
                        rs.getString("size"),
                        rs.getInt("qty_on_hand"),
                        rs.getInt("days_since")
                ),
                days, limit
        );
    }

    private SalesSummaryDto mapSalesSummary(ResultSet rs) throws SQLException {
        return new SalesSummaryDto(
                rs.getInt("order_count"),
                rs.getDouble("total_revenue"),
                rs.getDouble("total_tax")
        );
    }
}
