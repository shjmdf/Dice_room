package app;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import backend.dice.DiceExpressionResult;
import backend.message.Message;
import backend.playercard.PlayerCard;
import backend.room.Room;
import backend.user.InviteCode;
import backend.user.User;
import backend.user.passwordHash.Sha256PasswordHasher;
import repository.DatabaseConnection;
import repository.DatabaseInitializer;
import repository.InviteCodeRepository;
import repository.MessageRepository;
import repository.PlayerCardRepository;
import repository.RoomRepository;
import repository.UserRepository;
import service.AdminInitialize;
import service.DiceService;
import service.InviteCodeService;
import service.MessageService;
import service.PlayerCardService;
import service.RoomService;
import service.UserService;

public class Main {
    public static void main(String[] args) throws Exception {
        try (Connection connection = DatabaseConnection.open("dice_room.db")) {
            DatabaseInitializer.initialize(connection, "src/database/schema.sql");

            UserRepository userRepository = new UserRepository(connection);
            InviteCodeRepository inviteCodeRepository = new InviteCodeRepository(connection);
            RoomRepository roomRepository = new RoomRepository(connection);
            MessageRepository messageRepository = new MessageRepository(connection);
            PlayerCardRepository playerCardRepository = new PlayerCardRepository(connection);

            InviteCodeService inviteCodeService = new InviteCodeService(inviteCodeRepository);
            UserService userService = new UserService(userRepository, inviteCodeService, new Sha256PasswordHasher());
            RoomService roomService = new RoomService(userService, roomRepository);
            PlayerCardService playerCardService = new PlayerCardService(playerCardRepository);
            DiceService diceService = new DiceService(playerCardService);
            MessageService messageService = new MessageService(roomService, messageRepository);

            if (!userService.hasAdmin()) {
                AdminInitialize.initialize(userService, "admin", "admin123", "管理员");
            }

            long suffix = System.currentTimeMillis();
            InviteCode inviteCode = inviteCodeService.generateInviteCode(
                    2,
                    new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000),
                    null);

            User alice = userService.registerUser("alice_" + suffix, "password123", "Alice", inviteCode.getCode());
            User bob = userService.registerUser("bob_" + suffix, "password123", "Bob", inviteCode.getCode());

            Room room = roomService.createRoom(alice.getId(), "今晚 COC");
            roomService.joinRoom(bob.getId(), room.getRoomCode());

            int cardId = playerCardService.createPlayerCard(alice.getId(), "调查员 Alice");
            PlayerCard card = playerCardService.requireOwnedPlayerCard(alice.getId(), cardId);

            messageService.sendTextMessage(room.getId(), alice.getId(), "今晚开始调查。");
            DiceExpressionResult rollResult = diceService.roll("1d100");
            messageService.sendDiceMessage(room.getId(), alice.getId(),
                    rollResult.getExpression() + " = " + rollResult.getFinalResult());

            List<Message> messages = messageService.getVisibleMessages(room.getId(), bob.getId());

            System.out.println("管理员已存在: " + userService.hasAdmin());
            System.out.println("邀请码: " + inviteCode.getCode());
            System.out.println("用户: " + alice.getNickname() + ", " + bob.getNickname());
            System.out.println("房间: " + room.getName() + " / " + room.getRoomCode());
            System.out.println("角色卡: " + card.getName());
            System.out.println("Bob 可见消息数: " + messages.size());
            for (Message message : messages) {
                System.out.println(message.getType() + ": " + message.getContent());
            }
        }
    }
}
