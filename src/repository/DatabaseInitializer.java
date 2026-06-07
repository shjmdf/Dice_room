package repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize(Connection connection, String schemaPath) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接不能为空");
        }
        if (schemaPath == null || schemaPath.isBlank()) {
            throw new IllegalArgumentException("schema 路径不能为空");
        }

        try {
            String schema = Files.readString(Path.of(schemaPath));
            executeSqlScript(connection, schema);
        } catch (IOException exception) {
            throw new IllegalStateException("读取 schema 文件失败", exception);
        }
    }

    private static void executeSqlScript(Connection connection, String sqlScript) {
        String[] statements = sqlScript.split(";");

        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                String trimmedSql = sql.trim();
                if (!trimmedSql.isEmpty()) {
                    statement.execute(trimmedSql);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("执行 schema 失败", exception);
        }
    }
}
