package edu.icet.repository;

import edu.icet.model.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportRepository {
    SalesSummaryDto getSalesSummary(LocalDate from, LocalDate to);
    InventorySummaryDto getInventorySummary(int lowStockThreshold);
    List<TopProductDto> getTopProducts(LocalDate from, LocalDate to, int limit);
    List<DailySalesDto> getDailySales(LocalDate from, LocalDate to);
    List<MonthlySalesDto> getMonthlySales(LocalDate from, LocalDate to);
    ProfitSummaryDto getProfitSummary(LocalDate from, LocalDate to);
    List<CategoryRevenueDto> getRevenueByCategory(LocalDate from, LocalDate to);
    List<SlowMovingDto> getSlowMovingProducts(int days, int limit);
}
