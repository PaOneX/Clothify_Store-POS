package edu.icet.service.Impl;

import edu.icet.config.AppConfig;
import edu.icet.model.dto.*;
import edu.icet.repository.ReportRepository;
import edu.icet.service.OrderService;
import edu.icet.service.ReportService;

import java.time.LocalDate;
import java.util.List;

public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final OrderService orderService;

    public ReportServiceImpl(ReportRepository reportRepository, OrderService orderService) {
        this.reportRepository = reportRepository;
        this.orderService = orderService;
    }

    @Override
    public SalesSummaryDto getSalesSummary(LocalDate from, LocalDate to) {
        return reportRepository.getSalesSummary(from, to);
    }

    @Override
    public InventorySummaryDto getInventorySummary() {
        return reportRepository.getInventorySummary(AppConfig.getLowStockThreshold());
    }

    @Override
    public List<TopProductDto> getTopProducts(LocalDate from, LocalDate to) {
        return reportRepository.getTopProducts(from, to, 10);
    }

    @Override
    public List<DailySalesDto> getDailySales(LocalDate from, LocalDate to) {
        return reportRepository.getDailySales(from, to);
    }

    @Override
    public List<MonthlySalesDto> getMonthlySales(LocalDate from, LocalDate to) {
        return reportRepository.getMonthlySales(from, to);
    }

    @Override
    public ProfitSummaryDto getProfitSummary(LocalDate from, LocalDate to) {
        return reportRepository.getProfitSummary(from, to);
    }

    @Override
    public List<CategoryRevenueDto> getRevenueByCategory(LocalDate from, LocalDate to) {
        return reportRepository.getRevenueByCategory(from, to);
    }

    @Override
    public List<SlowMovingDto> getSlowMovingProducts(int days) {
        return reportRepository.getSlowMovingProducts(days, 20);
    }

    @Override
    public List<OrderDto> searchOrders(LocalDate from, LocalDate to, Integer orderId, Integer cashierId) {
        return orderService.searchOrders(from, to, orderId, cashierId);
    }
}
