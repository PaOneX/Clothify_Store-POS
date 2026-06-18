package edu.icet.service;

import edu.icet.model.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    SalesSummaryDto getSalesSummary(LocalDate from, LocalDate to);
    InventorySummaryDto getInventorySummary();
    List<TopProductDto> getTopProducts(LocalDate from, LocalDate to);
    List<DailySalesDto> getDailySales(LocalDate from, LocalDate to);
    List<MonthlySalesDto> getMonthlySales(LocalDate from, LocalDate to);
    ProfitSummaryDto getProfitSummary(LocalDate from, LocalDate to);
    List<CategoryRevenueDto> getRevenueByCategory(LocalDate from, LocalDate to);
    List<SlowMovingDto> getSlowMovingProducts(int days);
    List<OrderDto> searchOrders(LocalDate from, LocalDate to, Integer orderId, Integer cashierId);
}
