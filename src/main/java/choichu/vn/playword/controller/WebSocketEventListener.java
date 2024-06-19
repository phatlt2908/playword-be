package choichu.vn.playword.controller;

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

  public WebSocketEventListener(MultiWordLinkService multiWordLinkService) {
    this.multiWordLinkService = multiWordLinkService;
  }

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    log.info("Received a new web socket connection {}", event);
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

    String userId = (String) headerAccessor.getSessionAttributes().get("userId");
    String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
    if (userId != null && roomId != null) {
      multiWordLinkService.leaveRoom(userId, roomId);
    } else {
      log.error("Can not determine user and room");
    }
  }
}
