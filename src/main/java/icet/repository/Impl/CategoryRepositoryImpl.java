package edu.icet.repository.Impl;

import edu.icet.model.dto.CategoryDto;
import edu.icet.repository.CategoryRepository;
import edu.icet.util.CrudUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CategoryRepositoryImpl implements CategoryRepository {

    @Override
    public List<CategoryDto> findAll() {
        return CrudUtil.executeQueryForList("SELECT * FROM category ORDER BY category_id", this::mapRow);
    }

    @Override
    public Optional<CategoryDto> findById(Integer id) {
        return CrudUtil.executeQueryForOptional(
                "SELECT * FROM category WHERE category_id = ?",
                this::mapRow,
                id
        );
    }

    @Override
    public Integer save(CategoryDto entity) {
        return CrudUtil.executeUpdateWithGeneratedKeys(
                "INSERT INTO category (category_name, description) VALUES (?,?)",
                entity.getCategoryName(),
                entity.getDescription()
        );
    }

    @Override
    public void update(CategoryDto entity) {
        CrudUtil.executeUpdate(
                "UPDATE category SET category_name=?, description=? WHERE category_id=?",
                entity.getCategoryName(),
                entity.getDescription(),
                entity.getId()
        );
    }

    @Override
    public void deleteById(Integer id) {
        CrudUtil.executeUpdate("DELETE FROM category WHERE category_id = ?", id);
    }

    private CategoryDto mapRow(ResultSet rs) throws SQLException {
        return new CategoryDto(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("description")
        );
    }
}
