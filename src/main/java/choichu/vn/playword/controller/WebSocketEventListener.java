package choichu.vn.playword.controller;

import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.dto.multiwordlink.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class WebSocketEventListener {

  @Autowired
  private SimpMessageSendingOperations messagingTemplate;

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    log.info("Received a new web socket connection {}", event);
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

    String userId = (String) headerAccessor.getSessionAttributes().get("userId");
    if (userId != null) {
      log.info("User Disconnected : {}", userId);

      MessageDTO message = new MessageDTO();
      message.setType(MessageType.LEAVE);
//      message.setSender(username);

      messagingTemplate.convertAndSend("/room/public", message);
    }
  }
}
