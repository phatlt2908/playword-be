package choichu.vn.playword.controller;

import choichu.vn.playword.service.ChatService;
import choichu.vn.playword.service.MultiWordLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class WebSocketEventListener {

  private final MultiWordLinkService multiWordLinkService;
  private final ChatService chatService;

  public WebSocketEventListener(MultiWordLinkService multiWordLinkService,
                                ChatService chatService) {
    this.multiWordLinkService = multiWordLinkService;
    this.chatService = chatService;
  }

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    log.info("Received a new web socket connection {}", event);
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

    String userCode = (String) headerAccessor.getSessionAttributes().get("userCode");
    String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
    String chatUser = (String) headerAccessor.getSessionAttributes().get("chatUser");
    if (userCode != null && roomId != null) {
      multiWordLinkService.leaveRoom(userCode, roomId);
    }
    if (chatUser != null) {
      chatService.leave(chatUser);
    }
  }
}
