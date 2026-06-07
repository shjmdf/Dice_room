package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import backend.message.Message;
import backend.message.MessageType;
import backend.message.MessageVisibility;
import repository.Page;

public class MessageRepository {
    private final Connection connection;

    public MessageRepository(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接不能为空");
        }
        this.connection = connection;
    }

    public Message insert(int roomId, int senderId, int receiverId,
            MessageType type, MessageVisibility visibility, String content) {
        String sql = """
                INSERT INTO messages (room_id, sender_id, receiver_id, type, visibility, content)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, roomId);
            if (senderId == 0) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, senderId);
            }
            if (receiverId == 0) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, receiverId);
            }
            statement.setString(4, type.name());
            statement.setString(5, visibility.name());
            statement.setString(6, content);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new IllegalStateException("创建消息失败：没有返回消息 id");
                }
                return new Message(generatedKeys.getInt(1), roomId, senderId, receiverId, type, visibility, content);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("创建消息失败", exception);
        }
    }

    public Message findById(int messageId) {
        String sql = "SELECT * FROM messages WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, messageId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRowToMessage(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("查询消息失败", exception);
        }
    }

    public List<Message> findByRoomId(int roomId) {
        String sql = "SELECT * FROM messages WHERE room_id = ? ORDER BY id";
        List<Message> messages = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    messages.add(mapRowToMessage(resultSet));
                }
            }
            return messages;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询房间消息失败", exception);
        }
    }

    public List<Message> findVisibleMessages(int roomId, int userId) {
        return findVisibleMessages(roomId, userId, 1, 200).items();
    }

    public Page<Message> findVisibleMessages(int roomId, int userId, int page, int size) {
        int total = countVisibleMessages(roomId, userId);
        int offset = (page - 1) * size;

        String sql = """
                SELECT *
                FROM (
                    SELECT *
                    FROM messages
                    WHERE room_id = ?
                      AND (
                        visibility = 'PUBLIC'
                        OR sender_id = ?
                        OR receiver_id = ?
                      )
                    ORDER BY id DESC
                    LIMIT ? OFFSET ?
                ) latest_messages
                ORDER BY id
                """;
        List<Message> messages = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            statement.setInt(2, userId);
            statement.setInt(3, userId);
            statement.setInt(4, size);
            statement.setInt(5, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    messages.add(mapRowToMessage(resultSet));
                }
            }
            return new Page<>(messages, page, size, total);
        } catch (SQLException exception) {
            throw new IllegalStateException("查询可见消息失败", exception);
        }
    }

    private int countVisibleMessages(int roomId, int userId) {
        String sql = """
                SELECT COUNT(*)
                FROM messages
                WHERE room_id = ?
                  AND (
                    visibility = 'PUBLIC'
                    OR sender_id = ?
                    OR receiver_id = ?
                  )
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            statement.setInt(2, userId);
            statement.setInt(3, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("统计可见消息数量失败", exception);
        }
    }

    public List<Message> findPrivateMessages(int roomId, int userAId, int userBId) {
        String sql = """
                SELECT *
                FROM messages
                WHERE room_id = ?
                  AND visibility = 'PRIVATE'
                  AND (
                    (sender_id = ? AND receiver_id = ?)
                    OR
                    (sender_id = ? AND receiver_id = ?)
                  )
                ORDER BY id
                """;
        List<Message> messages = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            statement.setInt(2, userAId);
            statement.setInt(3, userBId);
            statement.setInt(4, userBId);
            statement.setInt(5, userAId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    messages.add(mapRowToMessage(resultSet));
                }
            }
            return messages;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询私聊消息失败", exception);
        }
    }

    public void deleteById(int messageId) {
        String sql = "DELETE FROM messages WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, messageId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalArgumentException("消息不存在");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("删除消息失败", exception);
        }
    }

    private Message mapRowToMessage(ResultSet resultSet) throws SQLException {
        int senderId = resultSet.getInt("sender_id");
        if (resultSet.wasNull()) {
            senderId = 0;
        }

        int receiverId = resultSet.getInt("receiver_id");
        if (resultSet.wasNull()) {
            receiverId = 0;
        }

        return new Message(
                resultSet.getInt("id"),
                resultSet.getInt("room_id"),
                senderId,
                receiverId,
                MessageType.valueOf(resultSet.getString("type")),
                MessageVisibility.valueOf(resultSet.getString("visibility")),
                resultSet.getString("content"));
    }
}
