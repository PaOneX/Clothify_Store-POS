package edu.icet.service;

import edu.icet.service.custom.Impl.ProductServiceImpl;
import edu.icet.service.custom.Impl.SupplierServiceImpl;
import edu.icet.service.custom.ProductService;
import edu.icet.util.ServiceType;

public class ServiceFactory {
    private static ServiceFactory instance;

    private ServiceFactory(){}

    public static ServiceFactory getInstance(){
        return instance == null ? instance = new ServiceFactory() : instance;
    }

    public <T extends SuperService> T getServiceType(ServiceType serviceType){
        switch (serviceType){
            case PRODUCTS: return (T)new ProductServiceImpl();
            case SUPPLIER: return (T)new SupplierServiceImpl();
        }
        return null;
    }


}
