package edu.icet.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    ID save(T entity);
    void update(T entity);
    void deleteById(ID id);
}
