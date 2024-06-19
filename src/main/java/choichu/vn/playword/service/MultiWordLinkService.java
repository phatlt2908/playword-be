package choichu.vn.playword.service;

import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.constant.RoomStatus;
import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import choichu.vn.playword.dto.multiwordlink.ResponseDTO;
import choichu.vn.playword.dto.multiwordlink.RoomDTO;
import choichu.vn.playword.dto.multiwordlink.SenderDTO;
import choichu.vn.playword.dto.multiwordlink.UserDTO;
import choichu.vn.playword.form.multiwordlink.MessageForm;
import java.util.Comparator;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MultiWordLinkService {

  private final DictionaryService dictionaryService;
  private final SimpMessageSendingOperations messagingTemplate;
  private final RedisTemplate<String, RoomDTO> redisTemplate;

  public MultiWordLinkService(DictionaryService dictionaryService,
                              SimpMessageSendingOperations messagingTemplate,
                              RedisTemplate<String, RoomDTO> redisTemplate) {
    this.dictionaryService = dictionaryService;
    this.messagingTemplate = messagingTemplate;
    this.redisTemplate = redisTemplate;
  }

  public RoomDTO addUserToRoom(MessageForm messageForm) {
    RoomDTO room = this.findRoomById(messageForm.getRoomId());
    if (room == null) {
      room = new RoomDTO();
      room.setId(messageForm.getRoomId());
      room.setName(messageForm.getRoomName());
      room.setStatus(RoomStatus.PREPARING);

      UserDTO user = new UserDTO();
      user.setId(messageForm.getSender().getId());
      user.setName(messageForm.getSender().getName());
      user.setOrder(1);
      user.setIsReady(false);

      room.getUserList().add(user);

      this.saveRoom(room);
    }
    else {
      UserDTO user = new UserDTO();
      user.setId(messageForm.getSender().getId());
      user.setName(messageForm.getSender().getName());
      user.setIsReady(false);

      int maxOrder = room.getUserList().stream()
                         .mapToInt(UserDTO::getOrder) // Extract only the value
                         .max()
                         .orElse(-1);
      user.setOrder(maxOrder + 1);

      room.getUserList().add(user);
      this.saveRoom(room);
    }

    return room;
  }

  public ResponseDTO answer(MessageForm messageForm) {
    ResponseDTO resMessage = new ResponseDTO();
    resMessage.setType(MessageType.ANSWER);
    resMessage.setUser(messageForm.getSender());

    RoomDTO room = this.findRoomById(messageForm.getRoomId());
    if (room == null || RoomStatus.PREPARING.equals(room.getStatus())) {
      log.error("Room is not found or not started yet. RoomId: {}", messageForm.getRoomId());
      resMessage.setIsAnswerCorrect(false);
      return resMessage;
    }

    UserDTO user = room.getUserList().stream()
                       .filter(u -> u.getId().equals(messageForm.getSender().getId()))
                       .findFirst()
                       .orElse(null);
    if (user == null || !Boolean.TRUE.equals(user.getIsReady()) || !Boolean.TRUE.equals(
        user.getIsAnswering())) {
      log.error("User is not found or not ready or not answering. UserId: {}",
                messageForm.getSender().getId());
      resMessage.setIsAnswerCorrect(false);
      return resMessage;
    }

    // Check the message request is correct or not
    if (messageForm.getMessage() == null || messageForm.getMessage().isEmpty()) {
      log.error("Word is empty. UserId: {}", messageForm.getSender().getId());
      resMessage.setIsAnswerCorrect(false);
      return resMessage;
    }

    // Check the message is answered before
    if (room.getWordList().contains(messageForm.getMessage())) {
      log.error("Word is answered before. UserId: {}", messageForm.getSender().getId());
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

    this.saveRoom(room);

    resMessage.setIsAnswerCorrect(true);
    resMessage.setRoom(room);
    resMessage.setWord(wordChecked);

    return resMessage;
  }

  public ResponseDTO readyUser(MessageForm messageForm) {
    ResponseDTO resMessage = new ResponseDTO();
    resMessage.setType(MessageType.READY);
    resMessage.setUser(messageForm.getSender());

    RoomDTO room = this.findRoomById(messageForm.getRoomId());
    if (room == null || RoomStatus.STARTED.equals(room.getStatus())) {
      return null;
    }

    UserDTO user = room.getUserList().stream()
                       .filter(u -> u.getId().equals(messageForm.getSender().getId()))
                       .findFirst()
                       .orElse(null);
    if (user == null) {
      return null;
    }

    user.setIsReady(true);

    if (room.getUserList().size() > 1 &&
        room.getUserList().stream().allMatch(UserDTO::getIsReady)) {
      room.setStatus(RoomStatus.STARTED);

      WordDescriptionDTO wordDescription = dictionaryService.findARandomWordLink();
      room.getWordList().add(wordDescription.getWord());
      resMessage.setWord(wordDescription);

      Objects.requireNonNull(room.getUserList().stream()
                                 .min(Comparator.comparingInt(UserDTO::getOrder))
                                 .orElse(null)).setIsAnswering(true);
    }

    this.saveRoom(room);

    resMessage.setRoom(room);

    return resMessage;
  }

  public void leaveRoom(String userId, String roomId) {
    log.info("User {} is leaving room {}", userId, roomId);

    RoomDTO room = this.findRoomById(roomId);
    if (room == null) {
      return;
    }

    UserDTO user = room.getUserList().stream()
                       .filter(u -> u.getId().equals(userId))
                       .findFirst()
                       .orElse(null);
    if (user == null) {
      return;
    }

    room.getUserList().remove(user);

    if (room.getUserList().isEmpty()) {
      redisTemplate.delete(roomId);
      return;
    }

    ResponseDTO message = new ResponseDTO();

    if (room.getUserList().size() == 1 && RoomStatus.STARTED.equals(room.getStatus())) {
      this.resetRoom(room);
      message.setType(MessageType.END);
      message.setUser(new SenderDTO(room.getUserList().getFirst().getId(),
                                    room.getUserList().getFirst().getName()));
    } else {
      if (Boolean.TRUE.equals(user.getIsAnswering()) &&
          RoomStatus.STARTED.equals(room.getStatus())) {
        this.continueToNextUser(room, user);
      }
      message.setType(MessageType.LEAVE);
      message.setUser(new SenderDTO(user.getId(), user.getName()));
    }

    this.saveRoom(room);
    message.setRoom(room);

    messagingTemplate.convertAndSend("/room/" + roomId, message);
  }

  public void saveRoom(RoomDTO room) {
    redisTemplate.opsForValue().set(room.getId(), room);
  }

  private RoomDTO findRoomById(String id) {
    return redisTemplate.opsForValue().get(id);
  }

  private void continueToNextUser(RoomDTO room, UserDTO user) {
    UserDTO nextUser = room.getUserList().stream()
                           .filter(u -> u.getOrder() > user.getOrder() &&
                                        Boolean.TRUE.equals(u.getIsReady()))
                           .min(Comparator.comparingInt(UserDTO::getOrder))
                           .orElse(null);
    if (nextUser == null) {
      nextUser = room.getUserList().stream()
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

  private void resetRoom(RoomDTO room) {
    room.setStatus(RoomStatus.PREPARING);
    room.getUserList().forEach(u -> {
      u.setIsReady(false);
      u.setIsAnswering(false);
    });
    room.getWordList().clear();
  }
}
