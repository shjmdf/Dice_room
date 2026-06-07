package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import backend.playercard.PlayerCard;

public class PlayerCardRepository {
    private final Connection connection;

    public PlayerCardRepository(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接不能为空");
        }
        this.connection = connection;
    }

    public PlayerCard insert(int ownerId, String name, String era, String cardJson) {
        String sql = """
                INSERT INTO player_cards (owner_id, name, era, status, card_json)
                VALUES (?, ?, ?, 'ACTIVE', ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, ownerId);
            statement.setString(2, name);
            statement.setString(3, era == null ? "" : era);
            statement.setString(4, normalizeCardJson(cardJson));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new IllegalStateException("创建角色卡失败：没有返回角色卡 id");
                }

                PlayerCard playerCard = new PlayerCard(ownerId, generatedKeys.getInt(1), name);
                playerCard.setEra(era == null ? "" : era);
                return playerCard;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("创建角色卡失败", exception);
        }
    }

    public PlayerCard findById(int cardId) {
        String sql = "SELECT * FROM player_cards WHERE id = ? AND status != 'DELETED'";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, cardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRowToPlayerCard(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("查询角色卡失败", exception);
        }
    }

    public List<PlayerCard> findByOwnerId(int ownerId) {
        String sql = "SELECT * FROM player_cards WHERE owner_id = ? AND status != 'DELETED' ORDER BY id";
        List<PlayerCard> playerCards = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    playerCards.add(mapRowToPlayerCard(resultSet));
                }
            }
            return playerCards;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询用户角色卡失败", exception);
        }
    }

    public String findCardJsonById(int cardId) {
        String sql = "SELECT card_json FROM player_cards WHERE id = ? AND status != 'DELETED'";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, cardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString("card_json");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("查询角色卡 JSON 失败", exception);
        }
    }

    public void updateBasicInfo(int cardId, String name, String era) {
        String sql = """
                UPDATE player_cards
                SET name = ?,
                    era = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        executeUpdate(sql, name, era == null ? "" : era, cardId);
    }

    public void updateCardJson(int cardId, String cardJson) {
        String sql = """
                UPDATE player_cards
                SET card_json = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        executeUpdate(sql, normalizeCardJson(cardJson), cardId);
    }

    public void archive(int cardId) {
        updateStatus(cardId, "ARCHIVED");
    }

    public void softDelete(int cardId) {
        updateStatus(cardId, "DELETED");
    }

    private void updateStatus(int cardId, String status) {
        String sql = """
                UPDATE player_cards
                SET status = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        executeUpdate(sql, status, cardId);
    }

    private PlayerCard mapRowToPlayerCard(ResultSet resultSet) throws SQLException {
        PlayerCard playerCard = new PlayerCard(
                resultSet.getInt("owner_id"),
                resultSet.getInt("id"),
                resultSet.getString("name"));
        playerCard.setEra(resultSet.getString("era"));
        return playerCard;
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
            throw new IllegalStateException("角色卡数据库更新失败", exception);
        }
    }

    private String normalizeCardJson(String cardJson) {
        if (cardJson == null || cardJson.isBlank()) {
            return "{}";
        }
        return cardJson;
    }
}
