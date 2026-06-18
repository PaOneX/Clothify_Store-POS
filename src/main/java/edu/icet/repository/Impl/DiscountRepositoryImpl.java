package edu.icet.repository.Impl;

import edu.icet.model.dto.DiscountDto;
import edu.icet.model.enums.DiscountType;
import edu.icet.repository.DiscountRepository;
import edu.icet.util.CrudUtil;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DiscountRepositoryImpl implements DiscountRepository {

    @Override
    public int save(DiscountDto discount) {
        return CrudUtil.executeUpdateWithGeneratedKeys(
                "INSERT INTO discount (code, name, type, value, min_order, valid_from, valid_to, active) VALUES (?,?,?,?,?,?,?,?)",
                discount.getCode(),
                discount.getName(),
                discount.getType().name(),
                discount.getValue(),
                discount.getMinOrder() != null ? discount.getMinOrder() : 0,
                discount.getValidFrom() != null ? Date.valueOf(discount.getValidFrom()) : null,
                discount.getValidTo() != null ? Date.valueOf(discount.getValidTo()) : null,
                discount.getActive() != null && discount.getActive() ? 1 : 1
        );
    }

    @Override
    public void update(DiscountDto discount) {
        CrudUtil.executeUpdate(
                "UPDATE discount SET code=?, name=?, type=?, value=?, min_order=?, valid_from=?, valid_to=?, active=? WHERE discount_id=?",
                discount.getCode(),
                discount.getName(),
                discount.getType().name(),
                discount.getValue(),
                discount.getMinOrder(),
                discount.getValidFrom() != null ? Date.valueOf(discount.getValidFrom()) : null,
                discount.getValidTo() != null ? Date.valueOf(discount.getValidTo()) : null,
                discount.getActive() != null && discount.getActive() ? 1 : 0,
                discount.getDiscountId()
        );
    }

    @Override
    public void delete(Integer discountId) {
        CrudUtil.executeUpdate("DELETE FROM discount WHERE discount_id = ?", discountId);
    }

    @Override
    public List<DiscountDto> findAll() {
        return CrudUtil.executeQueryForList(
                "SELECT discount_id, code, name, type, value, min_order, valid_from, valid_to, active FROM discount ORDER BY name",
                this::mapRow
        );
    }

    @Override
    public Optional<DiscountDto> findById(Integer discountId) {
        return CrudUtil.executeQueryForOptional(
                "SELECT discount_id, code, name, type, value, min_order, valid_from, valid_to, active FROM discount WHERE discount_id = ?",
                this::mapRow,
                discountId
        );
    }

    @Override
    public Optional<DiscountDto> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return CrudUtil.executeQueryForOptional(
                "SELECT discount_id, code, name, type, value, min_order, valid_from, valid_to, active FROM discount WHERE code = ?",
                this::mapRow,
                code.trim().toUpperCase()
        );
    }

    private DiscountDto mapRow(ResultSet rs) throws SQLException {
        Date from = rs.getDate("valid_from");
        Date to = rs.getDate("valid_to");
        return new DiscountDto(
                rs.getInt("discount_id"),
                rs.getString("code"),
                rs.getString("name"),
                DiscountType.valueOf(rs.getString("type")),
                rs.getDouble("value"),
                rs.getDouble("min_order"),
                from != null ? from.toLocalDate() : null,
                to != null ? to.toLocalDate() : null,
                rs.getInt("active") == 1
        );
    }
}
