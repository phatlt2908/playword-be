package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonStringConstant;
import choichu.vn.playword.constant.DictionaryApiUrlConstant;
import choichu.vn.playword.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = "*")
@RequestMapping(value = CommonStringConstant.BASE_API_URL)
@RestController
@Slf4j
public class DictionaryController {

  private final DictionaryService dictionaryService;

  public DictionaryController(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  @GetMapping(value = DictionaryApiUrlConstant.FIND_A_RANDOM_WORD, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> findARandomWord() {
    return dictionaryService.findARandomWord(2);
  }
}
