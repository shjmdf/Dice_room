package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private DatabaseConnection() {
    }

    public static Connection open(String databasePath) {
        if (databasePath == null || databasePath.isBlank()) {
            throw new IllegalArgumentException("数据库路径不能为空");
        }

        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            enableForeignKeys(connection);
            return connection;
        } catch (SQLException exception) {
            throw new IllegalStateException("打开数据库连接失败", exception);
        }
    }

    private static void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }
}
