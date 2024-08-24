package choichu.vn.playword.service;

import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.constant.RoomStatus;
import choichu.vn.playword.dto.RankingChartDTO;
import choichu.vn.playword.dto.RoomDTO;
import choichu.vn.playword.dto.SenderDTO;
import choichu.vn.playword.dto.UserDTO;
import choichu.vn.playword.dto.dictionary.BaseWordResponseDTO;
import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import choichu.vn.playword.dto.stick.MultiModeStickResponseDTO;
import choichu.vn.playword.dto.stick.StickWordDTO;
import choichu.vn.playword.form.multiwordlink.MessageForm;
import choichu.vn.playword.model.SingleRoomEntity;
import choichu.vn.playword.model.UserEntity;
import choichu.vn.playword.model.ViDictionaryEntity;
import choichu.vn.playword.repository.SingleRoomRepository;
import choichu.vn.playword.repository.UserRepository;
import choichu.vn.playword.repository.ViDictionaryRepository;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class StickService {

  private final RoomService roomService;
  private final ViDictionaryRepository viDictionaryRepository;
  private final UserRepository userRepository;
  private final SingleRoomRepository singleRoomRepository;
  private final SimpMessageSendingOperations messagingTemplate;

  public StickService(RoomService roomService,
                      ViDictionaryRepository viDictionaryRepository,
                      UserRepository userRepository,
                      SingleRoomRepository singleRoomRepository,
                      SimpMessageSendingOperations messagingTemplate) {
    this.roomService = roomService;
    this.viDictionaryRepository = viDictionaryRepository;
    this.userRepository = userRepository;
    this.singleRoomRepository = singleRoomRepository;
    this.messagingTemplate = messagingTemplate;
  }

  public StickWordDTO getAStickWord() {

    // Get a random word to start
    List<ViDictionaryEntity> wordList = viDictionaryRepository.findAllStickWord();

    if (CollectionUtils.isEmpty(wordList)) {
      log.error("Can not get a random stick word");
      return null;
    }
    ViDictionaryEntity word = wordList.get(new Random().nextInt(wordList.size()));

    // Split this word into separate characters (remove spaces)
    String[] characters = word.getWord().replaceAll("\\s+", "").split("");

    // Uppercase the first character
    characters[0] = characters[0].toUpperCase();

    // Random shuffle the characters in the string
    for (int i = 0; i < characters.length; i++) {
      int randomIndex = new Random().nextInt(characters.length);
      String temp = characters[i];
      characters[i] = characters[randomIndex];
      characters[randomIndex] = temp;
    }

    StickWordDTO stickWord = new StickWordDTO();
    stickWord.setCharacters(characters);
    stickWord.setWordBase64Encoded(Base64.getEncoder().encodeToString(word.getWord().getBytes()));
    stickWord.setDescriptionBase64Encoded(
        Base64.getEncoder().encodeToString(word.getDescription().getBytes(StandardCharsets.UTF_8)));

    return stickWord;
  }

  public BaseWordResponseDTO answer(String answer) {
    BaseWordResponseDTO response = new BaseWordResponseDTO();

    List<String> answerList = new ArrayList<>();

    // Lowercase the answer
    answer = answer.toLowerCase();

    for (int i = 1; i <= answer.length(); i++) {
      String modifiedString = answer.substring(0, i) + " " + answer.substring(i);
      answerList.add(modifiedString);
    }

    WordDescriptionDTO wordChecked =
        viDictionaryRepository.getWordByList(answerList,
                                             PageRequest.of(0, 1))
                              .stream()
                              .findFirst()
                              .map(w -> new WordDescriptionDTO(
                                  w.getWord(), w.getDescription()))
                              .orElse(null);

    if (wordChecked == null) {
      return response;
    }

    response.setIsSuccessful(true);
    response.setWordDescription(wordChecked);
    return response;
  }

  public ResponseEntity<?> getRank(Integer point, String userCode) {
    if (point == null || point <= 0) {
      return ResponseEntity.ok(0);
    }

    UserEntity user = userRepository.findByUserCode(userCode);

    SingleRoomEntity singleRoom = new SingleRoomEntity();
    singleRoom.setCreatedDate(new Date());
    singleRoom.setPoint(point);
    singleRoom.setUserId(user.getId());
    singleRoom.setGame(CommonConstant.KHAC_NHAP_GAME);
    singleRoomRepository.save(singleRoom);

    Integer rank = singleRoomRepository.getRank(point, CommonConstant.KHAC_NHAP_GAME);
    rank++;

    return ResponseEntity.ok(rank);
  }

  public ResponseEntity<?> getRankingChart(int top) {
    List<RankingChartDTO> rankingChartList =
        singleRoomRepository.getRankingChart(CommonConstant.KHAC_NHAP_GAME, PageRequest.of(0, top));

    for (int i = 0; i < rankingChartList.size(); i++) {
      if (i > 0 && rankingChartList.get(i).getPoint().equals(rankingChartList.get(i - 1).getPoint())) {
        rankingChartList.get(i).setRank(rankingChartList.get(i - 1).getRank());
        continue;
      }
      rankingChartList.get(i).setRank(i + 1);
    }

    return ResponseEntity.ok(rankingChartList);
  }

  public ResponseEntity<?> getUserRanking(String userCode) {

    RankingChartDTO userRanking = singleRoomRepository.getRankingChartByUserCode(
        CommonConstant.KHAC_NHAP_GAME, userCode);

    if (userRanking == null) {
      return ResponseEntity.ok(null);
    }

    Integer rank = singleRoomRepository.getRank(userRanking.getPoint(),
                                                CommonConstant.KHAC_NHAP_GAME);
    userRanking.setRank(rank + 1);

    return ResponseEntity.ok(userRanking);
  }

  public RoomDTO addUserToRoom(MessageForm messageForm) {
    return roomService.addUserToRoom(messageForm);
  }

  public MultiModeStickResponseDTO readyUser(MessageForm messageForm) {
    MultiModeStickResponseDTO resMessage = new MultiModeStickResponseDTO();
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

      StickWordDTO stickWord = this.getAStickWord();
      resMessage.setStickWord(stickWord);

      room.getUserList().forEach(u -> {
        u.setIsAnswering(true);
        u.setScore(0);
      });

      roomService.updateRoomToDB(room.getId(), true);
    }

    roomService.saveRoomToRedis(room);

    resMessage.setRoom(room);

    return resMessage;
  }

  public MultiModeStickResponseDTO answer(MessageForm messageForm) {
    MultiModeStickResponseDTO resMessage = new MultiModeStickResponseDTO();
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

    // The answer is incorrect
    BaseWordResponseDTO answerCheckingResponse = this.answer(messageForm.getMessage());
    if (!Boolean.TRUE.equals(answerCheckingResponse.getIsSuccessful())) {
      resMessage.setIsAnswerCorrect(false);
      resMessage.setMessage(messageForm.getMessage());
      return resMessage;
    }

    // The answer is correct
    user.setScore(user.getScore() + 1);

    if (user.getScore() >= 5) {
      resMessage.setType(MessageType.END);
      roomService.resetRoom(room);
    } else {
      resMessage.setMessage(answerCheckingResponse.getWordDescription().getWord() + ": "
                            + answerCheckingResponse.getWordDescription().getDescription());
      StickWordDTO stickWord = this.getAStickWord();
      resMessage.setStickWord(stickWord);
    }

    resMessage.setIsAnswerCorrect(true);
    resMessage.setRoom(room);

    roomService.saveRoomToRedis(room);

    return resMessage;
  }

  public MultiModeStickResponseDTO over() {
    MultiModeStickResponseDTO resMessage = new MultiModeStickResponseDTO();
    StickWordDTO stickWord = this.getAStickWord();
    resMessage.setType(MessageType.OVER);
    resMessage.setStickWord(stickWord);
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

    MultiModeStickResponseDTO message = new MultiModeStickResponseDTO();

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

        room.getUserList().forEach(u -> u.setIsAnswering(true));
        StickWordDTO stickWord = this.getAStickWord();
        message.setStickWord(stickWord);

        message.setType(MessageType.READY);
        message.setUser(new SenderDTO(user.getCode(), user.getName(), user.getAvatar()));
      }
      else {
        message.setType(MessageType.LEAVE);
        message.setUser(new SenderDTO(user.getCode(), user.getName(), user.getAvatar()));
      }
    }

    // Find a new key user
    if (room.getUserList().stream().noneMatch(UserDTO::getIsKey)) {
      UserDTO newKeyUser = room.getUserList().stream()
                             .min(Comparator.comparing(UserDTO::getOrder))
                             .orElse(null);
      if (newKeyUser != null) {
        newKeyUser.setIsKey(true);
      }
    }

    roomService.saveRoomToRedis(room);
    message.setRoom(room);

    messagingTemplate.convertAndSend("/room/" + roomId, message);
  }
}
