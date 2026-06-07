package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import backend.user.InviteCode;
import backend.user.InviteCodeStatus;

public class InviteCodeRepository {
    private final Connection connection;

    public InviteCodeRepository(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接不能为空");
        }
        this.connection = connection;
    }

    public InviteCode insert(String code, int usageLimit, Date expirationDate, Integer createdByUserId) {
        String sql = """
                INSERT INTO invite_codes (code, usage_limit, used_count, status, expiration_date, created_by_user_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, code);
            statement.setInt(2, usageLimit);
            statement.setInt(3, 0);
            statement.setString(4, InviteCodeStatus.ACTIVE.name());
            statement.setString(5, toDateText(expirationDate));
            if (createdByUserId == null) {
                statement.setNull(6, java.sql.Types.INTEGER);
            } else {
                statement.setInt(6, createdByUserId);
            }
            statement.executeUpdate();
            return new InviteCode(code, usageLimit, expirationDate);
        } catch (SQLException exception) {
            throw new IllegalStateException("创建邀请码失败", exception);
        }
    }

    public InviteCode findByCode(String code) {
        String sql = "SELECT * FROM invite_codes WHERE code = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRowToInviteCode(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("查询邀请码失败", exception);
        }
    }

    public List<InviteCode> findAll() {
        String sql = "SELECT * FROM invite_codes ORDER BY id";
        List<InviteCode> inviteCodes = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                inviteCodes.add(mapRowToInviteCode(resultSet));
            }
            return inviteCodes;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询邀请码列表失败", exception);
        }
    }

    public void updateUsage(String code, int usedCount, InviteCodeStatus status) {
        String sql = """
                UPDATE invite_codes
                SET used_count = ?,
                    status = ?
                WHERE code = ?
                """;
        executeUpdate(sql, usedCount, status.name(), code);
    }

    public void updateStatus(String code, InviteCodeStatus status) {
        String sql = "UPDATE invite_codes SET status = ? WHERE code = ?";
        executeUpdate(sql, status.name(), code);
    }

    public void deleteByCode(String code) {
        String sql = "DELETE FROM invite_codes WHERE code = ?";
        executeUpdate(sql, code);
    }

    private InviteCode mapRowToInviteCode(ResultSet resultSet) throws SQLException {
        String code = resultSet.getString("code");
        int usageLimit = resultSet.getInt("usage_limit");
        int usedCount = resultSet.getInt("used_count");
        InviteCodeStatus status = InviteCodeStatus.valueOf(resultSet.getString("status"));
        Date expirationDate = fromDateText(resultSet.getString("expiration_date"));
        return new InviteCode(code, usageLimit, usedCount, status, expirationDate);
    }

    private void executeUpdate(String sql, Object... params) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalArgumentException("没有找到要更新的数据");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("邀请码数据库更新失败", exception);
        }
    }

    private String toDateText(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        return date.toInstant().toString();
    }

    private Date fromDateText(String value) {
        return Date.from(Instant.parse(value));
    }
}
