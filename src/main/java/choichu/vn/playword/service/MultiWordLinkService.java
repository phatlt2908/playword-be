package choichu.vn.playword.service;

import choichu.vn.playword.constant.CommonStringConstant;
import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.constant.RoomStatus;
import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import choichu.vn.playword.dto.multiwordlink.BaseRoomInfoDTO;
import choichu.vn.playword.dto.multiwordlink.ResponseDTO;
import choichu.vn.playword.dto.multiwordlink.RoomDTO;
import choichu.vn.playword.dto.multiwordlink.SenderDTO;
import choichu.vn.playword.dto.multiwordlink.UserDTO;
import choichu.vn.playword.form.multiwordlink.MessageForm;
import choichu.vn.playword.model.RoomEntity;
import choichu.vn.playword.model.UserEntity;
import choichu.vn.playword.repository.RoomRepository;
import choichu.vn.playword.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MultiWordLinkService {

  private final DictionaryService dictionaryService;
  private final SimpMessageSendingOperations messagingTemplate;
  private final RedisTemplate<String, RoomDTO> redisTemplate;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;

  public MultiWordLinkService(DictionaryService dictionaryService,
                              SimpMessageSendingOperations messagingTemplate,
                              RedisTemplate<String, RoomDTO> redisTemplate,
                              RoomRepository roomRepository,
                              UserRepository userRepository) {
    this.dictionaryService = dictionaryService;
    this.messagingTemplate = messagingTemplate;
    this.redisTemplate = redisTemplate;
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
  }

  public ResponseEntity<List<BaseRoomInfoDTO>> getRoomList(String keyword) {
    List<BaseRoomInfoDTO> list = this.getAllRoom(keyword);
    return ResponseEntity.ok(list);
  }

  public ResponseEntity<String> findRoom() {
    List<BaseRoomInfoDTO> list = this.getAllRoom("");
    List<BaseRoomInfoDTO> preparingRoomList =
        list.stream()
            .filter(r -> RoomStatus.PREPARING.name().equals(r.getStatus())
                         && r.getUserCount() < 2
                         && CommonStringConstant.SOLO_ROOM_NAME.equals(r.getName()))
            .toList();
    if (preparingRoomList.isEmpty()) {
      return ResponseEntity.ok(null);
    }

    Random random = new Random();
    int randomIndex = random.nextInt(preparingRoomList.size());
    BaseRoomInfoDTO randomRoom = preparingRoomList.get(randomIndex);

    String result = randomRoom == null ? "" : randomRoom.getId();
    return ResponseEntity.ok(result);
  }

  public RoomDTO addUserToRoom(MessageForm messageForm) {
    RoomDTO room = this.findRoomById(messageForm.getRoomId());
    if (room == null) {
      room = new RoomDTO();
      room.setId(messageForm.getRoomId());
      room.setName(messageForm.getRoomName());
      room.setStatus(RoomStatus.PREPARING);

      UserDTO user = new UserDTO();
      user.setCode(messageForm.getSender().getCode());
      user.setName(messageForm.getSender().getName());
      user.setAvatar(messageForm.getSender().getAvatar());
      user.setOrder(1);
      user.setIsReady(false);

      room.getUserList().add(user);

    }
    else {
      if (room.getUserList().size() >= 2
          && CommonStringConstant.SOLO_ROOM_NAME.equals(room.getName())) {
        log.error("Room is full. RoomId: {}", messageForm.getRoomId());
        return null;
      }

      // Return null if userList has the same user
      if (room.getUserList().stream()
              .anyMatch(u -> u.getCode().equals(messageForm.getSender().getCode()))) {
        log.error("User is already in the room. UserCode: {}", messageForm.getSender().getCode());
        return null;
      }

      UserDTO user = new UserDTO();
      user.setCode(messageForm.getSender().getCode());
      user.setName(messageForm.getSender().getName());
      user.setAvatar(messageForm.getSender().getAvatar());
      user.setIsReady(false);

      int maxOrder = room.getUserList().stream()
                         .mapToInt(UserDTO::getOrder) // Extract only the value
                         .max()
                         .orElse(-1);
      user.setOrder(maxOrder + 1);

      room.getUserList().add(user);
    }

    this.saveRoomToRedis(room);
    this.updateRoomToDB(room.getId(), false);

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

    this.saveRoomToRedis(room);

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

      this.updateRoomToDB(room.getId(), true);
    }

    this.saveRoomToRedis(room);

    resMessage.setRoom(room);

    return resMessage;
  }

  public void leaveRoom(String userCode, String roomId) {
    log.info("User {} is leaving room {}", userCode, roomId);

    RoomDTO room = this.findRoomById(roomId);
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
      this.deleteRoom(roomId);
      return;
    }

    ResponseDTO message = new ResponseDTO();

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
        this.resetRoom(room);
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

    this.saveRoomToRedis(room);
    message.setRoom(room);

    messagingTemplate.convertAndSend("/room/" + roomId, message);
  }

  public ResponseDTO over(MessageForm message) {
    String userCode = message.getSender().getCode();
    String roomId = message.getRoomId();
    log.info("User {} in room {} is over game", userCode, roomId);

    RoomDTO room = this.findRoomById(message.getRoomId());
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

    ResponseDTO response = new ResponseDTO();

    // Get alive playing user list
    List<UserDTO> aliveUserList = room.getUserList().stream()
                                      .filter(u -> Boolean.TRUE.equals(u.getIsReady()))
                                      .toList();
    if (aliveUserList.size() == 1) {
      response.setType(MessageType.END);
      response.setUser(new SenderDTO(aliveUserList.getFirst().getCode(),
                                     aliveUserList.getFirst().getName(),
                                     aliveUserList.getFirst().getAvatar()));
      this.resetRoom(room);
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

    this.saveRoomToRedis(room);
    response.setRoom(room);

    return response;
  }

  public void createAnEmptyRoom(String roomId, String roomName, String userCode) {
    RoomDTO room = new RoomDTO();
    room.setId(roomId);
    room.setName(roomName);
    room.setStatus(RoomStatus.PREPARING);
    this.saveRoomToRedis(room);
    this.createRoomToDB(roomId, roomName, userCode);
  }

  public void saveRoomToRedis(RoomDTO room) {
    redisTemplate.opsForValue().set(room.getId(), room);
  }

  private void createRoomToDB(String id, String name, String userCode) {
    Optional<RoomEntity> optional = this.roomRepository.findById(id);
    RoomEntity roomEntity = optional.orElseGet(RoomEntity::new);

    UserEntity user = userRepository.findByUserCode(userCode);
    roomEntity.setId(id);
    roomEntity.setName(name);
    roomEntity.setCreatedDate(new Date());
    roomEntity.setCreatedBy(user.getId());
    roomEntity.setFinishedAt(null);
    roomEntity.setIsActive(true);
    roomEntity.setRound(0);
    this.roomRepository.save(roomEntity);
  }

  private void updateRoomToDB(String id, boolean isIncreaseRound) {
    Optional<RoomEntity> optional = this.roomRepository.findById(id);
    if (optional.isPresent()) {
      RoomEntity roomEntity = optional.get();
      roomEntity.setIsActive(true);
      roomEntity.setRound(isIncreaseRound ? roomEntity.getRound() + 1 : roomEntity.getRound());
      this.roomRepository.save(roomEntity);
    }
  }

  private void deactivateRoom(String id) {
    this.roomRepository.findById(id)
                       .ifPresent(roomEntity -> {
                         roomEntity.setIsActive(false);
                         roomEntity.setFinishedAt(new Date());
                         this.roomRepository.save(roomEntity);
                       });
  }

  private RoomDTO findRoomById(String id) {
    return redisTemplate.opsForValue().get(id);
  }

  private List<BaseRoomInfoDTO> getAllRoom(String keyword) {
    List<RoomEntity> roomList =
        this.roomRepository.search(keyword, PageRequest.of(0, 20));

    List<BaseRoomInfoDTO> baseRoomInfoList = new ArrayList<>();
    for (RoomEntity room : roomList) {
      RoomDTO roomDTO = redisTemplate.opsForValue().get(room.getId());
      if (roomDTO == null) {
        this.deleteRoom(room.getId());
        continue;
      }

      BaseRoomInfoDTO baseRoomInfo = new BaseRoomInfoDTO();
      baseRoomInfo.setId(room.getId());
      baseRoomInfo.setName(room.getName());
      baseRoomInfo.setUserCount(roomDTO.getUserList().size());
      baseRoomInfo.setStatus(roomDTO.getStatus().name());
      baseRoomInfoList.add(baseRoomInfo);
    }

    return baseRoomInfoList;
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

  private void deleteRoom(String roomId) {
    redisTemplate.delete(roomId);
    this.deactivateRoom(roomId);
    log.info("Room {} is deleted", roomId);
  }
}
