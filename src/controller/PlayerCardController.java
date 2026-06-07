package controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import backend.playercard.PlayerCard;
import backend.playercard.sheet.PlayerCardSheet;
import backend.user.User;
import service.AuthService;
import service.PlayerCardService;

@RestController
@RequestMapping("/api/player-cards")
public class PlayerCardController {
    private final PlayerCardService playerCardService;
    private final AuthService authService;

    public PlayerCardController(PlayerCardService playerCardService, AuthService authService) {
        this.playerCardService = playerCardService;
        this.authService = authService;
    }

    @PostMapping
    public PlayerCardSheetResponse createPlayerCard(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody CreatePlayerCardRequest request) {
        User currentUser = authService.requireUser(authorization);
        int cardId = playerCardService.createPlayerCard(currentUser.getId(), request.name());
        return getPlayerCardSheet(cardId, authorization);
    }

    @GetMapping("/{cardId}")
    public PlayerCard getPlayerCard(
            @PathVariable int cardId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        return requireAccessiblePlayerCard(authorization, cardId);
    }

    @GetMapping("/{cardId}/sheet")
    public PlayerCardSheetResponse getPlayerCardSheet(
            @PathVariable int cardId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        PlayerCard playerCard = requireAccessiblePlayerCard(authorization, cardId);
        PlayerCardSheet sheet = playerCardService.getPlayerCardSheet(playerCard.getOwnerId(), cardId);

        return new PlayerCardSheetResponse(
                playerCard.getId(),
                playerCard.getOwnerId(),
                playerCard.getName(),
                playerCard.getEra(),
                sheet);
    }

    @PutMapping("/{cardId}/sheet")
    public PlayerCardSheetResponse updatePlayerCardSheet(
            @PathVariable int cardId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody UpdatePlayerCardSheetRequest request) {
        PlayerCard playerCard = requireAccessiblePlayerCard(authorization, cardId);
        playerCardService.updatePlayerCardSheet(playerCard.getOwnerId(), cardId, request.sheet());
        return getPlayerCardSheet(cardId, authorization);
    }

    @GetMapping("/{cardId}/json")
    public PlayerCardSheet getCardJson(
            @PathVariable int cardId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        PlayerCard playerCard = requireAccessiblePlayerCard(authorization, cardId);
        return playerCardService.getPlayerCardSheet(playerCard.getOwnerId(), cardId);
    }

    @PutMapping("/{cardId}/json")
    public void replaceCardJson(
            @PathVariable int cardId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody UpdateCardJsonRequest request) {
        PlayerCard playerCard = requireAccessiblePlayerCard(authorization, cardId);
        playerCardService.updatePlayerCardSheet(playerCard.getOwnerId(), cardId, request.sheet());
    }

    @GetMapping("/user/{userId}")
    public List<PlayerCard> getPlayerCardsByUserId(
            @PathVariable int userId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireSelfOrAdmin(authorization, userId);
        return playerCardService.getPlayerCardsByOwnerId(userId);
    }

    @PatchMapping("/{cardId}/basic")
    public void updateBasicInfo(
            @PathVariable int cardId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody UpdateBasicInfoRequest request) {
        PlayerCard playerCard = requireAccessiblePlayerCard(authorization, cardId);
        playerCardService.updatePlayerCardBasicInfo(playerCard.getOwnerId(), cardId, request.name(), request.era());
    }

    @DeleteMapping("/{cardId}")
    public void deletePlayerCard(
            @PathVariable int cardId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        PlayerCard playerCard = requireAccessiblePlayerCard(authorization, cardId);
        playerCardService.deletePlayerCard(playerCard.getOwnerId(), cardId);
    }

    private PlayerCard requireAccessiblePlayerCard(String authorization, int cardId) {
        User currentUser = authService.requireUser(authorization);
        PlayerCard playerCard = playerCardService.requirePlayerCard(cardId);
        if (playerCard.getOwnerId() != currentUser.getId() && !currentUser.isAdmin()) {
            throw new IllegalStateException("无权操作该角色卡");
        }
        return playerCard;
    }

    public record CreatePlayerCardRequest(String name) {
    }

    public record UpdateBasicInfoRequest(String name, String era) {
    }

    public record UpdateCardJsonRequest(PlayerCardSheet sheet) {
    }

    public record UpdatePlayerCardSheetRequest(PlayerCardSheet sheet) {
    }

    public record PlayerCardSheetResponse(int id, int ownerId, String name, String era, PlayerCardSheet sheet) {
    }
}
