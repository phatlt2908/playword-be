package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonStringConstant;
import choichu.vn.playword.constant.StickApiUrlConstant;
import choichu.vn.playword.service.StickService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = "*")
@RequestMapping(value = CommonStringConstant.BASE_API_URL)
@RestController
@Slf4j
public class StickController {

  private StickService stickService;

  public StickController(StickService stickService) {
    this.stickService = stickService;
  }

  @GetMapping(value = StickApiUrlConstant.GET_WORD, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getWord() {
    return stickService.getWord();
  }

  @GetMapping(value = StickApiUrlConstant.ANSWER, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> answer(String answer) {
    return stickService.answer(answer);
  }

  @GetMapping(value = StickApiUrlConstant.RESULT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> result(@RequestParam Integer point, @RequestParam String userCode) {
    return stickService.getRank(point, userCode);
  }

  @GetMapping(value = StickApiUrlConstant.RANKING_CHART, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getRankingChart(@RequestParam int top) {
    return stickService.getRankingChart(top);
  }

  @GetMapping(value = StickApiUrlConstant.USER_RANKING, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getUserRanking(@RequestParam String userCode) {
    return stickService.getUserRanking(userCode);
  }
}
