package edu.icet.util;

import edu.icet.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class CrudUtil {

    private CrudUtil() {
    }

    public static void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    public static int executeUpdate(String sql, Object... params) {
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            return executeUpdate(connection, sql, params);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    public static int executeUpdate(Connection connection, String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update failed: " + sql, e);
        }
    }

    public static int executeUpdateWithGeneratedKeys(String sql, Object... params) {
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            return executeUpdateWithGeneratedKeys(connection, sql, params);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    public static int executeUpdateWithGeneratedKeys(Connection connection, String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Update with keys failed: " + sql, e);
        }
    }

    public static <T> List<T> executeQueryForList(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            return executeQueryForList(connection, sql, mapper, params);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    public static <T> List<T> executeQueryForList(Connection connection, String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query list failed: " + sql, e);
        }
        return results;
    }

    public static <T> Optional<T> executeQueryForOptional(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            return executeQueryForOptional(connection, sql, mapper, params);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    public static <T> Optional<T> executeQueryForOptional(Connection connection, String sql, RowMapper<T> mapper, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query optional failed: " + sql, e);
        }
        return Optional.empty();
    }

    public static <T> T executeInTransaction(Function<Connection, T> callback) {
        Connection connection = DBConnection.getInstance().getConnectionUnchecked();
        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            T result = callback.apply(connection);
            connection.commit();
            return result;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to restore connection", e);
            }
        }
    }
}
