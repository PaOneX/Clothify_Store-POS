package edu.icet.factory;

import edu.icet.repository.*;
import edu.icet.repository.Impl.*;

public final class RepositoryFactory {

    private static RepositoryFactory instance;

    private final ProductRepository productRepository = new ProductRepositoryImpl();
    private final ProductVariantRepository productVariantRepository = new ProductVariantRepositoryImpl();
    private final SupplierRepository supplierRepository = new SupplierRepositoryImpl();
    private final CategoryRepository categoryRepository = new CategoryRepositoryImpl();
    private final EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
    private final UserRepository userRepository = new UserRepositoryImpl();
    private final CustomerRepository customerRepository = new CustomerRepositoryImpl();
    private final DiscountRepository discountRepository = new DiscountRepositoryImpl();
    private final InventoryRepository inventoryRepository = new InventoryRepositoryImpl();
    private final OrderRepository orderRepository = new OrderRepositoryImpl();
    private final InvoiceRepository invoiceRepository = new InvoiceRepositoryImpl();
    private final ReturnRepository returnRepository = new ReturnRepositoryImpl();
    private final ReportRepository reportRepository = new ReportRepositoryImpl();
    private final AuditRepository auditRepository = new AuditRepositoryImpl();
    private final NotificationRepository notificationRepository = new NotificationRepositoryImpl();

    private RepositoryFactory() {
    }

    public static RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }

    public ProductRepository getProductRepository() { return productRepository; }
    public ProductVariantRepository getProductVariantRepository() { return productVariantRepository; }
    public SupplierRepository getSupplierRepository() { return supplierRepository; }
    public CategoryRepository getCategoryRepository() { return categoryRepository; }
    public EmployeeRepository getEmployeeRepository() { return employeeRepository; }
    public UserRepository getUserRepository() { return userRepository; }
    public CustomerRepository getCustomerRepository() { return customerRepository; }
    public DiscountRepository getDiscountRepository() { return discountRepository; }
    public InventoryRepository getInventoryRepository() { return inventoryRepository; }
    public OrderRepository getOrderRepository() { return orderRepository; }
    public InvoiceRepository getInvoiceRepository() { return invoiceRepository; }
    public ReturnRepository getReturnRepository() { return returnRepository; }
    public ReportRepository getReportRepository() { return reportRepository; }
    public AuditRepository getAuditRepository() { return auditRepository; }
    public NotificationRepository getNotificationRepository() { return notificationRepository; }
}
