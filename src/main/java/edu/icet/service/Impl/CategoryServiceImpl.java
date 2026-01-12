package edu.icet.service.Impl;

import edu.icet.model.dto.CategoryDto;
import edu.icet.repository.CategoryRepository;
import edu.icet.service.CategoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public int addCategory(CategoryDto categoryDto) {
        return repository.save(categoryDto);
    }

    @Override
    public void updateCategory(CategoryDto categoryDto) {
        repository.update(categoryDto);
    }

    @Override
    public void deleteCategory(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public ObservableList<CategoryDto> getAllCategories() {
        return FXCollections.observableArrayList(repository.findAll());
    }
}
