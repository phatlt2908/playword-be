package choichu.vn.playword.controller;

import choichu.vn.playword.dto.RoomDTO;
import choichu.vn.playword.service.ChatService;
import choichu.vn.playword.service.MultiWordLinkService;
import choichu.vn.playword.service.RoomService;
import choichu.vn.playword.service.StickService;
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
  private final StickService stickService;
  private final ChatService chatService;
  private final RoomService roomService;

  public WebSocketEventListener(MultiWordLinkService multiWordLinkService,
                                StickService stickService,
                                ChatService chatService,
                                RoomService roomService) {
    this.multiWordLinkService = multiWordLinkService;
    this.stickService = stickService;
    this.chatService = chatService;
    this.roomService = roomService;
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
      RoomDTO room = roomService.findRoomById(roomId);
      if (room.getGame() == 1) {
        multiWordLinkService.leaveRoom(userCode, roomId);
      }
      else if (room.getGame() == 2) {
        stickService.leaveRoom(userCode, roomId);
      } else {
        multiWordLinkService.leaveRoom(userCode, roomId);
        stickService.leaveRoom(userCode, roomId);
      }
    }
    if (chatUser != null) {
      chatService.leave(chatUser);
    }
  }
}
