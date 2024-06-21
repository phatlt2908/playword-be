package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonStringConstant;
import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.dto.multiwordlink.ResponseDTO;
import choichu.vn.playword.dto.multiwordlink.RoomDTO;
import choichu.vn.playword.form.multiwordlink.MessageForm;
import choichu.vn.playword.service.MultiWordLinkService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = "*")
@RequestMapping(value = CommonStringConstant.BASE_API_URL)
@RestController
@Controller
@Slf4j
public class MultiWordLinkController {

  private final MultiWordLinkService multiWordLinkService;

  public MultiWordLinkController(MultiWordLinkService multiWordLinkService) {
    this.multiWordLinkService = multiWordLinkService;
  }

  @MessageMapping("/addUser/{roomId}")
  @SendTo("/room/{roomId}")
  public ResponseDTO addUser(@DestinationVariable String roomId,
                             @Payload MessageForm message,
                             SimpMessageHeaderAccessor headerAccessor) {
    RoomDTO room = multiWordLinkService.addUserToRoom(message);

    // Add userId and roomId in web socket session
    if (Objects.nonNull(headerAccessor.getSessionAttributes())) {
      headerAccessor.getSessionAttributes().put("userId", message.getSender().getId());
      headerAccessor.getSessionAttributes().put("roomId", room.getId());
    }

    ResponseDTO resMessage = new ResponseDTO();
    resMessage.setType(MessageType.JOIN);
    resMessage.setUser(message.getSender());
    resMessage.setRoom(room);

    return resMessage;
  }

  @MessageMapping("/ready/{roomId}")
  @SendTo("/room/{roomId}")
  public ResponseDTO ready(@DestinationVariable String roomId,
                           @Payload MessageForm message,
                           SimpMessageHeaderAccessor headerAccessor) {
    return multiWordLinkService.readyUser(message);
  }

  @MessageMapping("/answer/{roomId}")
  @SendTo("/room/{roomId}")
  public ResponseDTO answer(@DestinationVariable String roomId,
                            @Payload MessageForm message,
                            SimpMessageHeaderAccessor headerAccessor) {
    return multiWordLinkService.answer(message);
  }

  @MessageMapping("/over/{roomId}")
  @SendTo("/room/{roomId}")
  public ResponseDTO over(@DestinationVariable String roomId,
                          @Payload MessageForm message,
                          SimpMessageHeaderAccessor headerAccessor) {
    return multiWordLinkService.over(message);
  }
}
