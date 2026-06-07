package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "app",
        "controller",
        "service",
        "repository",
        "backend"
})
public class DiceRoomApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiceRoomApplication.class, args);
    }
}
