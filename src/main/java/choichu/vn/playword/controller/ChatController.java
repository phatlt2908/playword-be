package choichu.vn.playword.controller;

import choichu.vn.playword.constant.ChatApiUrlConstant;
import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.dto.chat.ChatDTO;
import choichu.vn.playword.form.chat.ChatForm;
import choichu.vn.playword.service.ChatService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = "*")
@RequestMapping(value = CommonConstant.BASE_API_URL)
@RestController
@Slf4j
public class ChatController {

  private ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  @MessageMapping("/join")
  @SendTo("/room/chat-room")
  public ChatDTO join(@Payload ChatForm form, SimpMessageHeaderAccessor headerAccessor) {

    // Add chatUser in web socket session
    if (Objects.nonNull(headerAccessor.getSessionAttributes())) {
      headerAccessor.getSessionAttributes().put("chatUser", form.getUserCode());
    }

    return chatService.join(form.getUserCode());
  }

  @MessageMapping("/chat")
  @SendTo("/room/chat-room")
  public ChatDTO chat(@Payload ChatForm form) {
    return chatService.chat(form);
  }

  @GetMapping(value = ChatApiUrlConstant.LIST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getChatList(@RequestParam Long largestId) {
    return chatService.getHistoryChatList(largestId);
  }
}
