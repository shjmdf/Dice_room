package service;

import java.util.List;

import backend.playercard.PlayerCard;
import backend.playercard.PlayerCardSheetTemplate;
import backend.playercard.sheet.PlayerCardSheet;
import backend.playercard.sheet.PlayerCardSheetMapper;
import backend.playercard.sheet.PlayerCardSheetMapper.SkillValue;
import repository.PlayerCardRepository;

/*
 * 角色卡服务类，负责角色卡创建、查询、归属校验和删除。
 *
 * 角色卡内部修改仍然通过 PlayerCard 下的子对象完成。
 */
public class PlayerCardService {
    private final PlayerCardRepository playerCardRepository;
    private final UserService userService;

    public PlayerCardService(PlayerCardRepository playerCardRepository, UserService userService) {
        if (playerCardRepository == null) {
            throw new IllegalArgumentException("角色卡仓库不能为空");
        }
        if (userService == null) {
            throw new IllegalArgumentException("用户服务不能为空");
        }
        this.playerCardRepository = playerCardRepository;
        this.userService = userService;
    }

    public int createPlayerCard(int userId, String name) {
        userService.requireActiveUser(userId);
        PlayerCard defaultCard = new PlayerCard(userId, 0, name);
        PlayerCard playerCard = playerCardRepository.insert(userId, defaultCard.getName(), defaultCard.getEra(),
                PlayerCardSheetTemplate.createSheet(defaultCard));
        playerCardRepository.updateCardJson(playerCard.getId(), PlayerCardSheetTemplate.createSheet(playerCard));
        return playerCard.getId();
    }

    public List<PlayerCard> getPlayerCardsByOwnerId(int userId) {
        userService.requireActiveUser(userId);
        return playerCardRepository.findByOwnerId(userId);
    }

    public PlayerCard findPlayerCardById(int cardId) {
        return playerCardRepository.findById(cardId);
    }

    public PlayerCard requirePlayerCard(int cardId) {
        PlayerCard playerCard = findPlayerCardById(cardId);
        if (playerCard == null) {
            throw new IllegalArgumentException("角色卡不存在");
        }
        return playerCard;
    }

    public PlayerCard requireOwnedPlayerCard(int userId, int cardId) {
        userService.requireActiveUser(userId);
        PlayerCard playerCard = requirePlayerCard(cardId);
        if (playerCard.getOwnerId() != userId) {
            throw new IllegalStateException("无权操作该角色卡");
        }
        return playerCard;
    }

    public PlayerCardSheet getPlayerCardSheet(int userId, int cardId) {
        requireOwnedPlayerCard(userId, cardId);
        String cardJson = playerCardRepository.findCardJsonById(cardId);
        if (cardJson == null || cardJson.isBlank() || "{}".equals(cardJson.trim())) {
            PlayerCard playerCard = requireOwnedPlayerCard(userId, cardId);
            PlayerCardSheet sheet = PlayerCardSheetTemplate.createSheetObject(playerCard);
            playerCardRepository.updateCardJson(cardId, PlayerCardSheetMapper.toJson(sheet));
            return sheet;
        }

        PlayerCardSheet sheet = PlayerCardSheetMapper.fromJson(cardJson);
        PlayerCardSheetMapper.normalizeIdentity(sheet, userId, cardId);
        return sheet;
    }

    public void updatePlayerCardSheet(int userId, int cardId, PlayerCardSheet sheet) {
        PlayerCard playerCard = requireOwnedPlayerCard(userId, cardId);
        PlayerCardSheetMapper.normalizeIdentity(sheet, userId, cardId);

        String name = PlayerCardSheetMapper.getSummaryName(sheet, playerCard.getName());
        String era = PlayerCardSheetMapper.getSummaryEra(sheet);
        playerCardRepository.updateBasicInfo(cardId, name, era);
        playerCardRepository.updateCardJson(cardId, PlayerCardSheetMapper.toJson(sheet));
    }

    public void updatePlayerCardJson(int userId, int cardId, String cardJson) {
        PlayerCardSheet sheet = PlayerCardSheetMapper.fromJson(cardJson);
        updatePlayerCardSheet(userId, cardId, sheet);
    }

    public String getPlayerCardJson(int userId, int cardId) {
        return PlayerCardSheetMapper.toJson(getPlayerCardSheet(userId, cardId));
    }

    public void updatePlayerCardSheet(int userId, int cardId, String name, String era, String cardJson) {
        PlayerCardSheet sheet = PlayerCardSheetMapper.fromJson(cardJson);
        sheet.getBasicInformation().setName(name);
        sheet.getBasicInformation().setEra(era);
        sheet.setEra(era);
        updatePlayerCardSheet(userId, cardId, sheet);
    }

    public void updatePlayerCardBasicInfo(int userId, int cardId, String name, String era) {
        PlayerCard playerCard = requireOwnedPlayerCard(userId, cardId);
        playerCard.setName(name);
        playerCard.setEra(era);
        playerCardRepository.updateBasicInfo(cardId, name, era);
    }

    public void deletePlayerCard(int userId, int cardId) {
        requireOwnedPlayerCard(userId, cardId);
        playerCardRepository.softDelete(cardId);
    }

    public boolean isPlayerCardOwnedByUser(int userId, int cardId) {
        PlayerCard playerCard = findPlayerCardById(cardId);
        return playerCard != null && playerCard.getOwnerId() == userId;
    }

    public SkillValue getSkillValue(int userId, int cardId, String skillName) {
        PlayerCardSheet sheet = getPlayerCardSheet(userId, cardId);
        return PlayerCardSheetMapper.toSkillValue(PlayerCardSheetMapper.requireSkill(sheet, skillName));
    }
}
