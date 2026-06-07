package backend.dice;

public class DiceError extends RuntimeException {
    public DiceError(String message) {
        super(message);
    }
}
