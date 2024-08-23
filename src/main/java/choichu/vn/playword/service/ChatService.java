package choichu.vn.playword.service;

import choichu.vn.playword.constant.ChatType;
import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.dto.SenderDTO;
import choichu.vn.playword.dto.chat.ChatDTO;
import choichu.vn.playword.dto.chat.HistoryChatDTO;
import choichu.vn.playword.form.chat.ChatForm;
import choichu.vn.playword.model.ChatEntity;
import choichu.vn.playword.model.UserEntity;
import choichu.vn.playword.repository.ChatRepository;
import choichu.vn.playword.repository.UserRepository;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  private final UserRepository userRepository;
  private final RedisTemplate<String, String> chatRedisTemplate;
  private final SimpMessageSendingOperations messagingTemplate;
  private final ChatRepository chatRepository;

  public ChatService(UserRepository userRepository,
                     RedisTemplate<String, String> chatRedisTemplate,
                     SimpMessageSendingOperations messagingTemplate,
                     ChatRepository chatRepository) {
    this.userRepository = userRepository;
    this.chatRedisTemplate = chatRedisTemplate;
    this.messagingTemplate = messagingTemplate;
    this.chatRepository = chatRepository;
  }

  public ChatDTO join(String userCode) {

    UserEntity user = userRepository.findByUserCode(userCode);
    String onlineCount =
        chatRedisTemplate.opsForValue().get(CommonConstant.ONLINE_COUNT_REDIS_KEY);
    int updatedOnlineCount = (onlineCount == null || Integer.parseInt(onlineCount) < 1) ? 1 :
                             Integer.parseInt(onlineCount) + 1;
    chatRedisTemplate.opsForValue().set(CommonConstant.ONLINE_COUNT_REDIS_KEY,
                                        String.valueOf(updatedOnlineCount));

    ChatDTO res = new ChatDTO();
    res.setType(ChatType.JOIN);
    res.setUser(new SenderDTO(user.getCode(), user.getName(), user.getAvatar()));
    res.setOnlineCount(updatedOnlineCount);

    return res;
  }

  public void leave(String userCode) {
    UserEntity user = userRepository.findByUserCode(userCode);
    String onlineCount =
        chatRedisTemplate.opsForValue().get(CommonConstant.ONLINE_COUNT_REDIS_KEY);
    int updatedOnlineCount = onlineCount == null ? 0 : Integer.parseInt(onlineCount) - 1;
    chatRedisTemplate.opsForValue().set(CommonConstant.ONLINE_COUNT_REDIS_KEY,
                                        String.valueOf(updatedOnlineCount));

    ChatDTO res = new ChatDTO();
    res.setType(ChatType.LEAVE);
    res.setUser(new SenderDTO(user.getCode(), user.getName(), user.getAvatar()));
    res.setOnlineCount(updatedOnlineCount);

    messagingTemplate.convertAndSend("/room/chat-room", res);
  }

  public ChatDTO chat(ChatForm form) {

    UserEntity user = userRepository.findByUserCode(form.getUserCode());

    // Save to DB
    ChatEntity chat = new ChatEntity();
    chat.setUserId(user.getId());
    chat.setMessage(form.getMessage());
    chat.setSentAt(new Date());
    chat.setIsDeleted(false);
    chatRepository.save(chat);

    ChatDTO res = new ChatDTO();
    res.setType(ChatType.CHAT);
    res.setUser(new SenderDTO(user.getCode(), user.getName(), user.getAvatar()));
    res.setMessage(form.getMessage());

    String onlineCount =
        chatRedisTemplate.opsForValue().get(CommonConstant.ONLINE_COUNT_REDIS_KEY);
    res.setOnlineCount(onlineCount != null ? Integer.parseInt(onlineCount) : 0);

    return res;
  }

  public ResponseEntity<?> getHistoryChatList(Long largestId) {
    List<ChatEntity> list = chatRepository.getByIdSmallerThan(
        largestId == null ? 0 : largestId, PageRequest.of(0, 10));

    List<HistoryChatDTO> historyChatList = list.stream().map(chat -> {
      UserEntity user = userRepository.findById(chat.getUserId()).orElse(null);
      return new HistoryChatDTO(chat.getId(),
                                new SenderDTO(user.getCode(), user.getName(), user.getAvatar()),
                                chat.getMessage(), chat.getSentAt());
    }).toList();

    return ResponseEntity.ok(historyChatList);
  }
}
