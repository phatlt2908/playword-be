package choichu.vn.playword.service;

import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.constant.RoomStatus;
import choichu.vn.playword.dto.RoomDTO;
import choichu.vn.playword.dto.SenderDTO;
import choichu.vn.playword.dto.UserDTO;
import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import choichu.vn.playword.dto.multiwordlink.MultiModeWordLinkResponseDTO;
import choichu.vn.playword.form.multiwordlink.MessageForm;
import choichu.vn.playword.repository.RoomRepository;
import choichu.vn.playword.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MultiWordLinkService {

  private final RoomService roomService;
  private final DictionaryService dictionaryService;
  private final SimpMessageSendingOperations messagingTemplate;
  private final RedisTemplate<String, RoomDTO> redisTemplate;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;

  public MultiWordLinkService(RoomService roomService,
                              DictionaryService dictionaryService,
                              SimpMessageSendingOperations messagingTemplate,
                              RedisTemplate<String, RoomDTO> redisTemplate,
                              RoomRepository roomRepository,
                              UserRepository userRepository) {
    this.roomService = roomService;
    this.dictionaryService = dictionaryService;
    this.messagingTemplate = messagingTemplate;
    this.redisTemplate = redisTemplate;
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
  }

  public RoomDTO addUserToRoom(MessageForm messageForm) {
    return roomService.addUserToRoom(messageForm);
  }

  public MultiModeWordLinkResponseDTO answer(MessageForm messageForm) {
    MultiModeWordLinkResponseDTO resMessage = new MultiModeWordLinkResponseDTO();
    resMessage.setType(MessageType.ANSWER);
    resMessage.setUser(messageForm.getSender());

    RoomDTO room = roomService.findRoomById(messageForm.getRoomId());
    if (room == null || RoomStatus.PREPARING.equals(room.getStatus())) {
      log.error("Room is not found or not started yet. RoomId: {}", messageForm.getRoomId());
      resMessage.setIsAnswerCorrect(false);
      return resMessage;
    }

    UserDTO user = room.getUserList().stream()
                       .filter(u -> u.getCode().equals(messageForm.getSender().getCode()))
                       .findFirst()
                       .orElse(null);
    if (user == null || !Boolean.TRUE.equals(user.getIsReady()) || !Boolean.TRUE.equals(
        user.getIsAnswering())) {
      log.error("User is not found or not ready or not answering. UserCode: {}",
                messageForm.getSender().getCode());
      resMessage.setIsAnswerCorrect(false);
      return resMessage;
    }

    // Check the message request is correct or not
    if (messageForm.getMessage() == null || messageForm.getMessage().isEmpty()) {
      log.error("Word is empty. UserCode: {}", messageForm.getSender().getCode());
      resMessage.setIsAnswerCorrect(false);
      return resMessage;
    }

    // Check the message is answered before
    if (room.getWordList().contains(messageForm.getMessage())) {
      log.error("Word is answered before. UserCode: {}", messageForm.getSender().getCode());
      resMessage.setIsAnswerCorrect(false);
      return resMessage;
    }

    WordDescriptionDTO wordChecked = dictionaryService.findAWord(messageForm.getMessage(), true);

    // The answer is incorrect
    if (wordChecked == null) {
      resMessage.setIsAnswerCorrect(false);
      return resMessage;
    }

    // The answer is correct
    room.getWordList().add(wordChecked.getWord());

    // Create a new thread to register the word to the database.
    new Thread(() -> dictionaryService.increaseUsedCount(messageForm.getMessage())).start();

    this.continueToNextUser(room, user);

    roomService.saveRoomToRedis(room);

    resMessage.setIsAnswerCorrect(true);
    resMessage.setRoom(room);
    resMessage.setWord(wordChecked);

    return resMessage;
  }

  public MultiModeWordLinkResponseDTO readyUser(MessageForm messageForm) {
    MultiModeWordLinkResponseDTO resMessage = new MultiModeWordLinkResponseDTO();
    resMessage.setType(MessageType.READY);
    resMessage.setUser(messageForm.getSender());

    RoomDTO room = roomService.findRoomById(messageForm.getRoomId());
    if (room == null || RoomStatus.STARTED.equals(room.getStatus())) {
      return null;
    }

    UserDTO user = room.getUserList().stream()
                       .filter(u -> u.getCode().equals(messageForm.getSender().getCode()))
                       .findFirst()
                       .orElse(null);
    if (user == null) {
      return null;
    }

    user.setIsReady(true);
    user.setName(messageForm.getSender().getName());
    user.setAvatar(messageForm.getSender().getAvatar());

    if (room.getUserList().size() > 1 &&
        room.getUserList().stream().allMatch(UserDTO::getIsReady)) {
      room.setStatus(RoomStatus.STARTED);

      WordDescriptionDTO wordDescription = dictionaryService.findARandomWordLink();
      room.getWordList().add(wordDescription.getWord());
      resMessage.setWord(wordDescription);

      Random random = new Random();
      int randomIndex = random.nextInt(room.getUserList().size());
      UserDTO randomUser = room.getUserList().get(randomIndex);
      randomUser.setIsAnswering(true);

      roomService.updateRoomToDB(room.getId(), true);
    }

    roomService.saveRoomToRedis(room);

    resMessage.setRoom(room);

    return resMessage;
  }

  public void leaveRoom(String userCode, String roomId) {
    log.info("User {} is leaving room {}", userCode, roomId);

    RoomDTO room = roomService.findRoomById(roomId);
    if (room == null) {
      log.error("[leaveRoom] Room is not found. RoomId: {}", roomId);
      throw new IllegalArgumentException("Room is not found.");
    }

    UserDTO user = room.getUserList().stream()
                       .filter(u -> u.getCode().equals(userCode))
                       .findFirst()
                       .orElse(null);
    if (user == null) {
      log.error("[leaveRoom] User is not found. UserCode: {}", userCode);
      throw new IllegalArgumentException("User is not found.");
    }

    room.getUserList().remove(user);

    if (room.getUserList().isEmpty()) {
      roomService.deleteRoom(roomId);
      return;
    }

    MultiModeWordLinkResponseDTO message = new MultiModeWordLinkResponseDTO();

    // Get alive playing user list
    List<UserDTO> aliveUserList = room.getUserList().stream()
                                      .filter(u -> Boolean.TRUE.equals(u.getIsReady()))
                                      .toList();
    if (aliveUserList.size() == 1) {
      if (RoomStatus.STARTED.equals(room.getStatus())) {
        message.setType(MessageType.END);
        message.setUser(new SenderDTO(aliveUserList.getFirst().getCode(),
                                      aliveUserList.getFirst().getName(),
                                      aliveUserList.getFirst().getAvatar()));
        roomService.resetRoom(room);
      }
      else {
        message.setType(MessageType.LEAVE);
        message.setUser(new SenderDTO(user.getCode(), user.getName(), user.getAvatar()));
      }
    }
    else {
      if (RoomStatus.PREPARING.equals(room.getStatus()) &&
          aliveUserList.size() == room.getUserList().size()) {
        room.setStatus(RoomStatus.STARTED);

        Objects.requireNonNull(room.getUserList().stream()
                                   .min(Comparator.comparingInt(UserDTO::getOrder))
                                   .orElse(null)).setIsAnswering(true);

        WordDescriptionDTO wordDescription = dictionaryService.findARandomWordLink();
        room.getWordList().add(wordDescription.getWord());
        message.setWord(wordDescription);
        message.setType(MessageType.READY);
        message.setUser(new SenderDTO(user.getCode(), user.getName(), user.getAvatar()));
      }
      else {
        if (Boolean.TRUE.equals(user.getIsAnswering()) &&
            RoomStatus.STARTED.equals(room.getStatus())) {
          this.continueToNextUser(room, user);

          WordDescriptionDTO wordDescription = dictionaryService.findARandomWordLink();
          room.getWordList().add(wordDescription.getWord());
          message.setWord(wordDescription);
        }
        message.setType(MessageType.LEAVE);
        message.setUser(new SenderDTO(user.getCode(), user.getName(), user.getAvatar()));
      }
    }

    roomService.saveRoomToRedis(room);
    message.setRoom(room);

    messagingTemplate.convertAndSend("/room/" + roomId, message);
  }

  public MultiModeWordLinkResponseDTO over(MessageForm message) {
    String userCode = message.getSender().getCode();
    String roomId = message.getRoomId();
    log.info("User {} in room {} is over game", userCode, roomId);

    RoomDTO room = roomService.findRoomById(message.getRoomId());
    if (room == null) {
      log.error("[over] Room is not found. RoomId: {}", roomId);
      throw new IllegalArgumentException("Room is not found.");
    }

    UserDTO user = room.getUserList().stream()
                       .filter(u -> u.getCode().equals(userCode))
                       .findFirst()
                       .orElse(null);
    if (user == null) {
      log.error("[over] User is not found. UserCode: {}", userCode);
      throw new IllegalArgumentException("User is not found.");
    }
    user.setIsReady(false);

    MultiModeWordLinkResponseDTO response = new MultiModeWordLinkResponseDTO();

    // Get alive playing user list
    List<UserDTO> aliveUserList = room.getUserList().stream()
                                      .filter(u -> Boolean.TRUE.equals(u.getIsReady()))
                                      .toList();
    if (aliveUserList.size() == 1) {
      response.setType(MessageType.END);
      response.setUser(new SenderDTO(aliveUserList.getFirst().getCode(),
                                     aliveUserList.getFirst().getName(),
                                     aliveUserList.getFirst().getAvatar()));
      roomService.resetRoom(room);
    }
    else {
      if (Boolean.TRUE.equals(user.getIsAnswering()) &&
          RoomStatus.STARTED.equals(room.getStatus())) {
        this.continueToNextUser(room, user);

        WordDescriptionDTO wordDescription = dictionaryService.findARandomWordLink();
        room.getWordList().add(wordDescription.getWord());
        response.setWord(wordDescription);
      }
      response.setType(MessageType.OVER);
      response.setUser(new SenderDTO(user.getCode(), user.getName(), user.getAvatar()));
    }

    roomService.saveRoomToRedis(room);
    response.setRoom(room);

    return response;
  }

  private void continueToNextUser(RoomDTO room, UserDTO user) {
    UserDTO nextUser = room.getUserList().stream()
                           .filter(u -> u.getOrder() > user.getOrder() &&
                                        Boolean.TRUE.equals(u.getIsReady()))
                           .min(Comparator.comparingInt(UserDTO::getOrder))
                           .orElse(null);
    if (nextUser == null) {
      nextUser = room.getUserList().stream()
                     .filter(u -> Boolean.TRUE.equals(u.getIsReady()))
                     .min(Comparator.comparingInt(UserDTO::getOrder))
                     .orElse(null);
    }
    if (nextUser == null) {
      log.error("Next user is not found.");
      nextUser = user;
    }

    user.setIsAnswering(false);
    nextUser.setIsAnswering(true);
  }
}
