package controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.dice.DiceExpressionResult;
import backend.dice.RepeatRollResult;
import backend.dice.SanCheckResult;
import backend.dice.SkillCheckResult;
import service.DiceService;

@RestController
@RequestMapping("/api/dice")
public class DiceController {
    private final DiceService diceService;

    public DiceController(DiceService diceService) {
        this.diceService = diceService;
    }

    @PostMapping("/roll")
    public DiceExpressionResult roll(@RequestBody RollRequest request) {
        return diceService.roll(request.expression());
    }

    @PostMapping("/repeat")
    public RepeatRollResult rollRepeated(@RequestBody RollRequest request) {
        return diceService.rollRepeated(request.expression());
    }

    @PostMapping("/san-check")
    public SanCheckResult sanCheck(@RequestBody SanCheckRequest request) {
        return diceService.sanCheck(request.args());
    }

    @PostMapping("/skill-check")
    public SkillCheckResult skillCheck(@RequestBody SkillCheckRequest request) {
        return diceService.checkSkill(request.userId(), request.cardId(), request.skillName());
    }

    public record RollRequest(String expression) {
    }

    public record SanCheckRequest(String args) {
    }

    public record SkillCheckRequest(int userId, int cardId, String skillName) {
    }
}
