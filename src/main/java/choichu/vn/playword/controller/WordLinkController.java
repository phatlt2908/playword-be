package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonStringConstant;
import choichu.vn.playword.constant.WordLinkApiUrlConstant;
import choichu.vn.playword.dto.multiwordlink.WordLinkMessage;
import choichu.vn.playword.service.WordLinkService;
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
@RequestMapping(value = CommonStringConstant.BASE_API_URL)
@RestController
@Controller
@Slf4j
public class WordLinkController {

  private final WordLinkService wordLinkService;

  public WordLinkController(WordLinkService wordLinkService) {
    this.wordLinkService = wordLinkService;
  }

  /**
   * Init to find a random word.
   * @return a random word.
   */
  @GetMapping(value = WordLinkApiUrlConstant.INIT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> init() {
    return wordLinkService.init();
  }

  /**
   * Answer to check.
   * @param word word to find.
   * @return word and description.
   */
  @GetMapping(value = WordLinkApiUrlConstant.ANSWER, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> answer(@RequestParam String word) {
    return wordLinkService.answer(word);
  }

  @MessageMapping("/chat.sendMessage/{roomId}")
  @SendTo("/room/{roomId}")
  public WordLinkMessage sendMessage(@DestinationVariable String roomId,
                                     @Payload WordLinkMessage chatMessage) {
    return chatMessage;
  }

  @MessageMapping("/chat.addUser/{roomId}")
  @SendTo("/room/{roomId}")
  public WordLinkMessage addUser(@DestinationVariable String roomId,
                                 @Payload WordLinkMessage chatMessage,
                                 SimpMessageHeaderAccessor headerAccessor) {
    // Add username in web socket session
    headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
    return chatMessage;
  }
}
