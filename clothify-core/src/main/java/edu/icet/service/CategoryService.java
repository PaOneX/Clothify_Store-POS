package edu.icet.service;

import edu.icet.model.dto.CategoryDto;
import javafx.collections.ObservableList;

public interface CategoryService {
    int addCategory(CategoryDto categoryDto);
    void updateCategory(CategoryDto categoryDto);
    void deleteCategory(Integer id);
    ObservableList<CategoryDto> getAllCategories();
}
