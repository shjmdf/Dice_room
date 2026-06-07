package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import backend.user.User;
import backend.user.UserRole;
import backend.user.UserStatus;

public class UserRepository {
    private final Connection connection;

    public UserRepository(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接不能为空");
        }
        this.connection = connection;
    }

    public User insert(String loginName, String passwordHash, String nickname, UserRole role) {
        String sql = """
                INSERT INTO users (login_name, password_hash, nickname, role, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, loginName);
            statement.setString(2, passwordHash);
            statement.setString(3, nickname);
            statement.setString(4, role.name());
            statement.setString(5, UserStatus.ACTIVE.name());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new IllegalStateException("创建用户失败：没有返回用户 id");
                }
                int id = generatedKeys.getInt(1);
                return new User(id, loginName, passwordHash, nickname, role);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("创建用户失败", exception);
        }
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRowToUser(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("按 id 查询用户失败", exception);
        }
    }

    public User findByLoginName(String loginName) {
        String sql = "SELECT * FROM users WHERE login_name = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, loginName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRowToUser(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("按登录名查询用户失败", exception);
        }
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapRowToUser(resultSet));
            }
            return users;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询用户列表失败", exception);
        }
    }

    public List<User> findAdmins() {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY id";
        List<User> admins = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, UserRole.ADMIN.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    admins.add(mapRowToUser(resultSet));
                }
            }
            return admins;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询管理员列表失败", exception);
        }
    }

    public boolean existsAdmin() {
        String sql = "SELECT 1 FROM users WHERE role = ? LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, UserRole.ADMIN.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("检查管理员是否存在失败", exception);
        }
    }

    public void updatePasswordHash(int userId, String passwordHash) {
        String sql = """
                UPDATE users
                SET password_hash = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        executeUpdate(sql, passwordHash, userId);
    }

    public void updateProfile(int userId, String nickname, String avatarUrl, String description, String email) {
        String sql = """
                UPDATE users
                SET nickname = ?,
                    avatar_url = ?,
                    description = ?,
                    email = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        executeUpdate(sql, nickname, avatarUrl, description, email, userId);
    }

    public void updateStatus(int userId, UserStatus status) {
        String sql = """
                UPDATE users
                SET status = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        executeUpdate(sql, status.name(), userId);
    }

    public void softDelete(int userId) {
        updateStatus(userId, UserStatus.DELETED);
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
            throw new IllegalStateException("数据库更新失败", exception);
        }
    }

    private User mapRowToUser(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String loginName = resultSet.getString("login_name");
        String passwordHash = resultSet.getString("password_hash");
        String nickname = resultSet.getString("nickname");
        UserRole role = UserRole.valueOf(resultSet.getString("role"));
        UserStatus status = UserStatus.valueOf(resultSet.getString("status"));

        User user = new User(id, loginName, passwordHash, nickname, role);
        applyStatus(user, status);
        user.setAvatarUrl(resultSet.getString("avatar_url"));
        user.setDescription(resultSet.getString("description"));
        user.setEmail(resultSet.getString("email"));
        return user;
    }

    private void applyStatus(User user, UserStatus status) {
        if (status == UserStatus.SUSPENDED) {
            user.suspend();
        } else if (status == UserStatus.DELETED) {
            user.delete();
        } else {
            user.recover();
        }
    }
}
