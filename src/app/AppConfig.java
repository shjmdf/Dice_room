package app;

import java.sql.Connection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import backend.user.passwordHash.PasswordHasher;
import backend.user.passwordHash.Sha256PasswordHasher;
import repository.DatabaseConnection;
import repository.DatabaseInitializer;
import repository.InviteCodeRepository;
import repository.MessageRepository;
import repository.PlayerCardRepository;
import repository.RoomRepository;
import repository.UserRepository;
import service.DiceService;
import service.InviteCodeService;
import service.MessageService;
import service.PlayerCardService;
import service.RoomService;
import service.UserService;

@Configuration
public class AppConfig {
    @Bean
    public Connection databaseConnection() {
        Connection connection = DatabaseConnection.open("dice_room.db");
        DatabaseInitializer.initialize(connection, "src/database/schema.sql");
        return connection;
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new Sha256PasswordHasher();
    }

    @Bean
    public UserRepository userRepository(Connection connection) {
        return new UserRepository(connection);
    }

    @Bean
    public InviteCodeRepository inviteCodeRepository(Connection connection) {
        return new InviteCodeRepository(connection);
    }

    @Bean
    public RoomRepository roomRepository(Connection connection) {
        return new RoomRepository(connection);
    }

    @Bean
    public MessageRepository messageRepository(Connection connection) {
        return new MessageRepository(connection);
    }

    @Bean
    public PlayerCardRepository playerCardRepository(Connection connection) {
        return new PlayerCardRepository(connection);
    }

    @Bean
    public InviteCodeService inviteCodeService(InviteCodeRepository inviteCodeRepository) {
        return new InviteCodeService(inviteCodeRepository);
    }

    @Bean
    public UserService userService(UserRepository userRepository,
            InviteCodeService inviteCodeService,
            PasswordHasher passwordHasher) {
        return new UserService(userRepository, inviteCodeService, passwordHasher);
    }

    @Bean
    public RoomService roomService(UserService userService, RoomRepository roomRepository) {
        return new RoomService(userService, roomRepository);
    }

    @Bean
    public PlayerCardService playerCardService(PlayerCardRepository playerCardRepository) {
        return new PlayerCardService(playerCardRepository);
    }

    @Bean
    public DiceService diceService(PlayerCardService playerCardService) {
        return new DiceService(playerCardService);
    }

    @Bean
    public MessageService messageService(RoomService roomService, MessageRepository messageRepository) {
        return new MessageService(roomService, messageRepository);
    }
}
