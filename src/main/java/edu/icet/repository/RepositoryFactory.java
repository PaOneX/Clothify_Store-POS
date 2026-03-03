package edu.icet.repository;

import edu.icet.repository.custom.Iml.ProductRepositoryImpl;
import edu.icet.repository.custom.Iml.SupplierRepositoryImpl;
import edu.icet.util.RepoositoryType;

public class RepositoryFactory {
    private static RepositoryFactory instance;

    private RepositoryFactory(){}

    public static RepositoryFactory getInstance(){
        return instance == null ? instance  = new RepositoryFactory() : instance;

    }

    public <T extends SuperRepository> T createRepositoryType(RepoositoryType type){
        switch(type){
            case PRODUCT: return (T) new ProductRepositoryImpl();
            case SUPPLIER: return (T) new SupplierRepositoryImpl();
        }
        return null;
    }

}
