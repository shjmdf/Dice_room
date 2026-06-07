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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.playercard.PlayerCard;
import backend.playercard.sheet.PlayerCardSheet;
import service.PlayerCardService;

@RestController
@RequestMapping("/api/player-cards")
public class PlayerCardController {
    private final PlayerCardService playerCardService;

    public PlayerCardController(PlayerCardService playerCardService) {
        this.playerCardService = playerCardService;
    }

    @PostMapping
    public PlayerCardSheetResponse createPlayerCard(@RequestBody CreatePlayerCardRequest request) {
        int cardId = playerCardService.createPlayerCard(request.userId(), request.name());
        return getPlayerCardSheet(cardId, request.userId());
    }

    @GetMapping("/{cardId}")
    public PlayerCard getPlayerCard(@PathVariable int cardId) {
        return playerCardService.requirePlayerCard(cardId);
    }

    @GetMapping("/{cardId}/sheet")
    public PlayerCardSheetResponse getPlayerCardSheet(@PathVariable int cardId, @RequestParam int userId) {
        PlayerCard playerCard = playerCardService.requireOwnedPlayerCard(userId, cardId);
        PlayerCardSheet sheet = playerCardService.getPlayerCardSheet(userId, cardId);

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
            @RequestBody UpdatePlayerCardSheetRequest request) {
        playerCardService.updatePlayerCardSheet(request.userId(), cardId, request.sheet());
        return getPlayerCardSheet(cardId, request.userId());
    }

    @GetMapping("/{cardId}/json")
    public PlayerCardSheet getCardJson(@PathVariable int cardId, @RequestParam int userId) {
        return playerCardService.getPlayerCardSheet(userId, cardId);
    }

    @PutMapping("/{cardId}/json")
    public void replaceCardJson(@PathVariable int cardId, @RequestBody UpdateCardJsonRequest request) {
        playerCardService.updatePlayerCardSheet(request.userId(), cardId, request.sheet());
    }

    @GetMapping("/user/{userId}")
    public List<PlayerCard> getPlayerCardsByUserId(@PathVariable int userId) {
        return playerCardService.getPlayerCardsByOwnerId(userId);
    }

    @PatchMapping("/{cardId}/basic")
    public void updateBasicInfo(@PathVariable int cardId, @RequestBody UpdateBasicInfoRequest request) {
        playerCardService.updatePlayerCardBasicInfo(request.userId(), cardId, request.name(), request.era());
    }

    @DeleteMapping("/{cardId}")
    public void deletePlayerCard(@PathVariable int cardId, @RequestParam int userId) {
        playerCardService.deletePlayerCard(userId, cardId);
    }

    public record CreatePlayerCardRequest(int userId, String name) {
    }

    public record UpdateBasicInfoRequest(int userId, String name, String era) {
    }

    public record UpdateCardJsonRequest(int userId, PlayerCardSheet sheet) {
    }

    public record UpdatePlayerCardSheetRequest(int userId, PlayerCardSheet sheet) {
    }

    public record PlayerCardSheetResponse(int id, int ownerId, String name, String era, PlayerCardSheet sheet) {
    }
}
