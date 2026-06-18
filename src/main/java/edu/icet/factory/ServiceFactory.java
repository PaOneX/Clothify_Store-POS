package edu.icet.factory;

import edu.icet.service.*;
import edu.icet.service.Impl.*;

public final class ServiceFactory {

    private static ServiceFactory instance;
    private final RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();

    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final SupplierService supplierService;
    private final CategoryService categoryService;
    private final EmployeeService employeeService;
    private final AuthService authService;
    private final CustomerService customerService;
    private final DiscountService discountService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final InvoiceService invoiceService;
    private final ReturnService returnService;
    private final ReportService reportService;
    private final JasperReportService jasperReportService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final BackupService backupService;

    private ServiceFactory() {
        productService = new ProductServiceImpl(repositoryFactory.getProductRepository());
        productVariantService = new ProductVariantServiceImpl(repositoryFactory.getProductVariantRepository());
        supplierService = new SupplierServiceImpl(repositoryFactory.getSupplierRepository());
        categoryService = new CategoryServiceImpl(repositoryFactory.getCategoryRepository());
        employeeService = new EmployeeServiceImpl(
                repositoryFactory.getEmployeeRepository(),
                repositoryFactory.getUserRepository()
        );
        authService = new AuthServiceImpl(
                repositoryFactory.getUserRepository(),
                new AuditServiceImpl(repositoryFactory.getAuditRepository())
        );
        customerService = new CustomerServiceImpl(
                repositoryFactory.getCustomerRepository(),
                repositoryFactory.getOrderRepository()
        );
        discountService = new DiscountServiceImpl(repositoryFactory.getDiscountRepository());
        inventoryService = new InventoryServiceImpl(
                repositoryFactory.getProductVariantRepository(),
                repositoryFactory.getInventoryRepository()
        );
        orderService = new OrderServiceImpl(
                repositoryFactory.getOrderRepository(),
                repositoryFactory.getProductVariantRepository(),
                repositoryFactory.getInventoryRepository(),
                repositoryFactory.getInvoiceRepository(),
                discountService
        );
        invoiceService = new InvoiceServiceImpl(
                repositoryFactory.getInvoiceRepository(),
                repositoryFactory.getOrderRepository()
        );
        returnService = new ReturnServiceImpl(
                repositoryFactory.getReturnRepository(),
                repositoryFactory.getProductVariantRepository(),
                repositoryFactory.getInventoryRepository()
        );
        reportService = new ReportServiceImpl(
                repositoryFactory.getReportRepository(),
                orderService
        );
        jasperReportService = new JasperReportServiceImpl();
        notificationService = new NotificationServiceImpl(
                repositoryFactory.getNotificationRepository(),
                repositoryFactory.getProductVariantRepository()
        );
        auditService = new AuditServiceImpl(repositoryFactory.getAuditRepository());
        backupService = new BackupServiceImpl();
    }

    public static ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    public ProductService getProductService() { return productService; }
    public ProductVariantService getProductVariantService() { return productVariantService; }
    public SupplierService getSupplierService() { return supplierService; }
    public CategoryService getCategoryService() { return categoryService; }
    public EmployeeService getEmployeeService() { return employeeService; }
    public AuthService getAuthService() { return authService; }
    public CustomerService getCustomerService() { return customerService; }
    public DiscountService getDiscountService() { return discountService; }
    public InventoryService getInventoryService() { return inventoryService; }
    public OrderService getOrderService() { return orderService; }
    public InvoiceService getInvoiceService() { return invoiceService; }
    public ReturnService getReturnService() { return returnService; }
    public ReportService getReportService() { return reportService; }
    public JasperReportService getJasperReportService() { return jasperReportService; }
    public NotificationService getNotificationService() { return notificationService; }
    public AuditService getAuditService() { return auditService; }
    public BackupService getBackupService() { return backupService; }
}
