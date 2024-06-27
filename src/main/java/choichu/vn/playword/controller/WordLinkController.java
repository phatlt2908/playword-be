package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonStringConstant;
import choichu.vn.playword.constant.WordLinkApiUrlConstant;
import choichu.vn.playword.service.WordLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.user.SimpUserRegistry;
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

  @Autowired
  private SimpUserRegistry simpUserRegistry;

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

  /**
   * Get rank.
   * @param point
   * @return rank.
   */
  @GetMapping(value = WordLinkApiUrlConstant.RESULT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> result(@RequestParam Integer point) {
    return wordLinkService.getRank(point);
  }
}
