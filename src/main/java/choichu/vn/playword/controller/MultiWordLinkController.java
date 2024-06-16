package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonStringConstant;
import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.constant.RoomStatus;
import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import choichu.vn.playword.dto.multiwordlink.MessageDTO;
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

  @MessageMapping("/sendMessage/{roomId}")
  @SendTo("/room/{roomId}")
  public MessageDTO sendMessage(@DestinationVariable String roomId,
                                @Payload MessageDTO chatMessage) {
    return chatMessage;
  }

  @MessageMapping("/addUser/{roomId}")
  @SendTo("/room/{roomId}")
  public MessageDTO addUser(@DestinationVariable String roomId,
                            @Payload MessageForm message,
                            SimpMessageHeaderAccessor headerAccessor) {
    // Add username in web socket session
    Objects.requireNonNull(headerAccessor.getSessionAttributes())
           .put("userId", message.getSender().getId());

    RoomDTO room = multiWordLinkService.addUserToRoom(message);

    MessageDTO resMessage = new MessageDTO();
    resMessage.setType(MessageType.JOIN);
    resMessage.setUser(message.getSender());
    resMessage.setRoom(room);

    return resMessage;
  }

  @MessageMapping("/ready/{roomId}")
  @SendTo("/room/{roomId}")
  public MessageDTO ready(@DestinationVariable String roomId,
                            @Payload MessageForm message,
                            SimpMessageHeaderAccessor headerAccessor) {
    RoomDTO room = multiWordLinkService.readyUser(message);

    MessageDTO resMessage = new MessageDTO();
    resMessage.setType(MessageType.READY);
    resMessage.setUser(message.getSender());
    resMessage.setRoom(room);

    if (RoomStatus.STARTED.equals(room.getStatus())) {
      WordDescriptionDTO wordDescription = dictionaryService.findARandomWordLink();
    }



    return resMessage;
  }
}
