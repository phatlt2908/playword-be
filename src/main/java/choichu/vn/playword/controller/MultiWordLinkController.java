package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.constant.WordLinkApiUrlConstant;
import choichu.vn.playword.dto.RoomDTO;
import choichu.vn.playword.dto.multiwordlink.MultiModeWordLinkResponseDTO;
import choichu.vn.playword.form.multiwordlink.MessageForm;
import choichu.vn.playword.service.MultiWordLinkService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = "*")
@RequestMapping(value = CommonConstant.BASE_API_URL)
@RestController
@Controller
@Slf4j
public class MultiWordLinkController {

  private final MultiWordLinkService multiWordLinkService;

  public MultiWordLinkController(MultiWordLinkService multiWordLinkService) {
    this.multiWordLinkService = multiWordLinkService;
  }

  @GetMapping(value = WordLinkApiUrlConstant.ROOM_LIST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getRoomList(@RequestParam String keyword) {
    return multiWordLinkService.getRoomList(keyword);
  }

  @GetMapping(value = WordLinkApiUrlConstant.CREATE_ROOM, produces =
      MediaType.APPLICATION_JSON_VALUE)
  public void createRoom(@RequestParam String id,
                         @RequestParam String name,
                         @RequestParam String userCode) {
    multiWordLinkService.createRoom(id, name, userCode);
  }

  @GetMapping(value = WordLinkApiUrlConstant.FIND_ROOM, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> findRoom() {
    return multiWordLinkService.findRoom();
  }

  @MessageMapping("/word-link/add-user/{roomId}")
  @SendTo("/room/{roomId}")
  public MultiModeWordLinkResponseDTO addUser(@DestinationVariable String roomId,
                                              @Payload MessageForm message,
                                              SimpMessageHeaderAccessor headerAccessor) {
    RoomDTO room = multiWordLinkService.addUserToRoom(message);

    if (room == null) {
      MultiModeWordLinkResponseDTO resMessage = new MultiModeWordLinkResponseDTO();
      resMessage.setType(MessageType.JOIN_FAIL);
      resMessage.setUser(message.getSender());
      return resMessage;
    }

    // Add userCode and roomId in web socket session
    if (Objects.nonNull(headerAccessor.getSessionAttributes())) {
      headerAccessor.getSessionAttributes().put("userCode", message.getSender().getCode());
      headerAccessor.getSessionAttributes().put("roomId", room.getId());
    }

    MultiModeWordLinkResponseDTO resMessage = new MultiModeWordLinkResponseDTO();
    resMessage.setType(MessageType.JOIN);
    resMessage.setUser(message.getSender());
    resMessage.setRoom(room);

    return resMessage;
  }

  @MessageMapping("/word-link/ready/{roomId}")
  @SendTo("/room/{roomId}")
  public MultiModeWordLinkResponseDTO ready(@DestinationVariable String roomId,
                                            @Payload MessageForm message,
                                            SimpMessageHeaderAccessor headerAccessor) {
    return multiWordLinkService.readyUser(message);
  }

  @MessageMapping("/word-link/answer/{roomId}")
  @SendTo("/room/{roomId}")
  public MultiModeWordLinkResponseDTO answer(@DestinationVariable String roomId,
                                             @Payload MessageForm message,
                                             SimpMessageHeaderAccessor headerAccessor) {
    return multiWordLinkService.answer(message);
  }

  @MessageMapping("/word-link/over/{roomId}")
  @SendTo("/room/{roomId}")
  public MultiModeWordLinkResponseDTO over(@DestinationVariable String roomId,
                                           @Payload MessageForm message,
                                           SimpMessageHeaderAccessor headerAccessor) {
    return multiWordLinkService.over(message);
  }
}
