package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import backend.room.Room;
import backend.room.RoomMember;
import backend.room.RoomRole;
import backend.room.RoomStatus;

public class RoomRepository {
    private final Connection connection;

    public RoomRepository(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接不能为空");
        }
        this.connection = connection;
    }

    public Room insert(String roomCode, int ownerId, String name) {
        String sql = """
                INSERT INTO rooms (room_code, owner_id, name, status)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, roomCode);
            statement.setInt(2, ownerId);
            statement.setString(3, name);
            statement.setString(4, RoomStatus.OPEN.name());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new IllegalStateException("创建房间失败：没有返回房间 id");
                }

                int roomId = generatedKeys.getInt(1);
                insertMember(roomId, ownerId, RoomRole.OWNER);
                return new Room(roomId, roomCode, ownerId, name);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("创建房间失败", exception);
        }
    }

    public Room findById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRowToRoom(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("按 id 查询房间失败", exception);
        }
    }

    public Room findByRoomCode(String roomCode) {
        String sql = "SELECT * FROM rooms WHERE room_code = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRowToRoom(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("按房间码查询房间失败", exception);
        }
    }

    public List<Room> findByUserId(int userId) {
        String sql = """
                SELECT rooms.*
                FROM rooms
                JOIN room_members ON room_members.room_id = rooms.id
                WHERE room_members.user_id = ?
                  AND room_members.left_at IS NULL
                ORDER BY rooms.id
                """;
        List<Room> rooms = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rooms.add(mapRowToRoom(resultSet));
                }
            }
            return rooms;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询用户房间列表失败", exception);
        }
    }

    public List<Room> findAll() {
        String sql = "SELECT * FROM rooms ORDER BY id";
        List<Room> rooms = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rooms.add(mapRowToRoom(resultSet));
            }
            return rooms;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询房间列表失败", exception);
        }
    }

    public void updateBasicInfo(int roomId, String name, String description, String tagsJson) {
        String sql = """
                UPDATE rooms
                SET name = ?,
                    description = ?,
                    tags_json = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        executeUpdate(sql, name, description, tagsJson, roomId);
    }

    public void updateStatus(int roomId, RoomStatus status) {
        String sql = """
                UPDATE rooms
                SET status = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        executeUpdate(sql, status.name(), roomId);
    }

    public RoomMember insertMember(int roomId, int userId, RoomRole role) {
        String sql = """
                INSERT INTO room_members (room_id, user_id, role, muted)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            statement.setInt(2, userId);
            statement.setString(3, role.name());
            statement.setInt(4, 0);
            statement.executeUpdate();
            return new RoomMember(roomId, userId, role);
        } catch (SQLException exception) {
            throw new IllegalStateException("添加房间成员失败", exception);
        }
    }

    public void leaveMember(int roomId, int userId) {
        String sql = """
                UPDATE room_members
                SET left_at = CURRENT_TIMESTAMP
                WHERE room_id = ?
                  AND user_id = ?
                  AND left_at IS NULL
                """;
        executeUpdate(sql, roomId, userId);
    }

    public void updateMemberRole(int roomId, int userId, RoomRole role) {
        String sql = """
                UPDATE room_members
                SET role = ?
                WHERE room_id = ?
                  AND user_id = ?
                  AND left_at IS NULL
                """;
        executeUpdate(sql, role.name(), roomId, userId);
    }

    public void updateMemberMuted(int roomId, int userId, boolean muted) {
        String sql = """
                UPDATE room_members
                SET muted = ?
                WHERE room_id = ?
                  AND user_id = ?
                  AND left_at IS NULL
                """;
        executeUpdate(sql, muted ? 1 : 0, roomId, userId);
    }

    public void updateMemberCard(int roomId, int userId, Integer cardId) {
        String sql = """
                UPDATE room_members
                SET card_id = ?
                WHERE room_id = ?
                  AND user_id = ?
                  AND left_at IS NULL
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (cardId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, cardId);
            }
            statement.setInt(2, roomId);
            statement.setInt(3, userId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalArgumentException("没有找到要更新的成员");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("绑定角色卡失败", exception);
        }
    }

    public void updateMemberDisplayName(int roomId, int userId, String displayName) {
        String sql = """
                UPDATE room_members
                SET display_name = ?
                WHERE room_id = ?
                  AND user_id = ?
                  AND left_at IS NULL
                """;
        executeUpdate(sql, displayName, roomId, userId);
    }

    public List<RoomMember> findActiveMembersByRoomId(int roomId) {
        String sql = """
                SELECT *
                FROM room_members
                WHERE room_id = ?
                  AND left_at IS NULL
                ORDER BY id
                """;
        List<RoomMember> members = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(mapRowToRoomMember(resultSet));
                }
            }
            return members;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询房间成员失败", exception);
        }
    }

    public boolean isActiveMember(int roomId, int userId) {
        String sql = """
                SELECT 1
                FROM room_members
                WHERE room_id = ?
                  AND user_id = ?
                  AND left_at IS NULL
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            statement.setInt(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("检查房间成员失败", exception);
        }
    }

    private Room mapRowToRoom(ResultSet resultSet) throws SQLException {
        Room room = new Room(
                resultSet.getInt("id"),
                resultSet.getString("room_code"),
                resultSet.getInt("owner_id"),
                resultSet.getString("name"));
        room.setDescription(resultSet.getString("description"));

        for (String tag : parseTagsJson(resultSet.getString("tags_json"))) {
            room.addTag(tag);
        }

        RoomStatus status = RoomStatus.valueOf(resultSet.getString("status"));
        if (status == RoomStatus.CLOSED) {
            room.close();
        } else if (status == RoomStatus.DELETED) {
            room.delete();
        }
        return room;
    }

    private RoomMember mapRowToRoomMember(ResultSet resultSet) throws SQLException {
        RoomMember member = new RoomMember(
                resultSet.getInt("room_id"),
                resultSet.getInt("user_id"),
                RoomRole.valueOf(resultSet.getString("role")));

        if (resultSet.getInt("muted") == 1) {
            member.mute();
        }

        int cardId = resultSet.getInt("card_id");
        if (!resultSet.wasNull()) {
            member.setCardId(cardId);
        }
        member.changeDisplayName(resultSet.getString("display_name"));
        return member;
    }

    private List<String> parseTagsJson(String tagsJson) {
        List<String> tags = new ArrayList<>();
        if (tagsJson == null || tagsJson.equals("[]")) {
            return tags;
        }

        String content = tagsJson.trim();
        if (content.startsWith("[") && content.endsWith("]")) {
            content = content.substring(1, content.length() - 1);
        }

        if (content.isBlank()) {
            return tags;
        }

        for (String item : content.split(",")) {
            String tag = item.trim();
            if (tag.startsWith("\"") && tag.endsWith("\"") && tag.length() >= 2) {
                tag = tag.substring(1, tag.length() - 1);
            }
            tag = tag.replace("\\\"", "\"");
            if (!tag.isBlank()) {
                tags.add(tag);
            }
        }
        return tags;
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
            throw new IllegalStateException("房间数据库更新失败", exception);
        }
    }
}
