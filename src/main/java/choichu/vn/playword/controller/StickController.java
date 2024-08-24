package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.constant.StickApiUrlConstant;
import choichu.vn.playword.dto.RoomDTO;
import choichu.vn.playword.dto.stick.MultiModeStickResponseDTO;
import choichu.vn.playword.form.multiwordlink.MessageForm;
import choichu.vn.playword.service.StickService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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
public class StickController {

  private StickService stickService;

  public StickController(StickService stickService) {
    this.stickService = stickService;
  }

  @GetMapping(value = StickApiUrlConstant.GET_WORD, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getWord() {
    return ResponseEntity.ok(stickService.getAStickWord());
  }

  @GetMapping(value = StickApiUrlConstant.ANSWER, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> answer(String answer) {
    return ResponseEntity.ok(stickService.answer(answer));
  }

  @GetMapping(value = StickApiUrlConstant.RESULT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> result(@RequestParam Integer point, @RequestParam String userCode) {
    return stickService.getRank(point, userCode);
  }

  @GetMapping(value = StickApiUrlConstant.RANKING_CHART, produces =
      MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getRankingChart(@RequestParam int top) {
    return stickService.getRankingChart(top);
  }

  @GetMapping(value = StickApiUrlConstant.USER_RANKING, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getUserRanking(@RequestParam String userCode) {
    return stickService.getUserRanking(userCode);
  }

  @MessageMapping("/stick/add-user/{roomId}")
  @SendTo("/room/{roomId}")
  public MultiModeStickResponseDTO addUser(@DestinationVariable String roomId,
                                           @Payload MessageForm message,
                                           SimpMessageHeaderAccessor headerAccessor) {
    RoomDTO room = stickService.addUserToRoom(message);

    if (room == null) {
      MultiModeStickResponseDTO resMessage = new MultiModeStickResponseDTO();
      resMessage.setType(MessageType.JOIN_FAIL);
      resMessage.setUser(message.getSender());
      return resMessage;
    }

    // Add userCode and roomId in web socket session
    if (Objects.nonNull(headerAccessor.getSessionAttributes())) {
      headerAccessor.getSessionAttributes().put("userCode", message.getSender().getCode());
      headerAccessor.getSessionAttributes().put("roomId", room.getId());
    }

    MultiModeStickResponseDTO resMessage = new MultiModeStickResponseDTO();
    resMessage.setType(MessageType.JOIN);
    resMessage.setUser(message.getSender());
    resMessage.setRoom(room);

    return resMessage;
  }

  @MessageMapping("/stick/ready/{roomId}")
  @SendTo("/room/{roomId}")
  public MultiModeStickResponseDTO ready(@DestinationVariable String roomId,
                                         @Payload MessageForm message,
                                         SimpMessageHeaderAccessor headerAccessor) {
    return stickService.readyUser(message);
  }

  @MessageMapping("/stick/answer/{roomId}")
  @SendTo("/room/{roomId}")
  public MultiModeStickResponseDTO answer(@DestinationVariable String roomId,
                                          @Payload MessageForm message,
                                          SimpMessageHeaderAccessor headerAccessor) {
    return stickService.answer(message);
  }

  @MessageMapping("/stick/over/{roomId}")
  @SendTo("/room/{roomId}")
  public MultiModeStickResponseDTO over(@DestinationVariable String roomId,
                                        @Payload MessageForm message,
                                        SimpMessageHeaderAccessor headerAccessor) {
    return stickService.over();
  }
}
