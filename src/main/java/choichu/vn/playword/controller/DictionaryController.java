package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonStringConstant;
import choichu.vn.playword.constant.DictionaryApiUrlConstant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = "*")
@RequestMapping(value = CommonStringConstant.BASE_API_URL)
@RestController
@AllArgsConstructor
@Slf4j
public class DictionaryController {
  @GetMapping(value = DictionaryApiUrlConstant.TEST, produces = MediaType.APPLICATION_JSON_VALUE)
  public String getListApproveCatalog() {
    return "Test ok";
  }
}
