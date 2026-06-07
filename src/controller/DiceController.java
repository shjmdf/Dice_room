package controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import backend.dice.DiceExpressionResult;
import backend.dice.RepeatRollResult;
import backend.dice.SanCheckResult;
import backend.dice.SkillCheckResult;
import backend.user.User;
import service.AuthService;
import service.DiceService;

@RestController
@RequestMapping("/api/dice")
public class DiceController {
    private final DiceService diceService;
    private final AuthService authService;

    public DiceController(DiceService diceService, AuthService authService) {
        this.diceService = diceService;
        this.authService = authService;
    }

    @PostMapping("/roll")
    public DiceExpressionResult roll(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody RollRequest request) {
        authService.requireUser(authorization);
        return diceService.roll(request.expression());
    }

    @PostMapping("/repeat")
    public RepeatRollResult rollRepeated(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody RollRequest request) {
        authService.requireUser(authorization);
        return diceService.rollRepeated(request.expression());
    }

    @PostMapping("/san-check")
    public SanCheckResult sanCheck(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody SanCheckRequest request) {
        authService.requireUser(authorization);
        return diceService.sanCheck(request.args());
    }

    @PostMapping("/skill-check")
    public SkillCheckResult skillCheck(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody SkillCheckRequest request) {
        User currentUser = authService.requireUser(authorization);
        return diceService.checkSkill(currentUser.getId(), request.cardId(), request.skillName());
    }

    public record RollRequest(String expression) {
    }

    public record SanCheckRequest(String args) {
    }

    public record SkillCheckRequest(int cardId, String skillName) {
    }
}
