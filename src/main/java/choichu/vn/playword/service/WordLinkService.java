package choichu.vn.playword.service;

import choichu.vn.playword.constant.MessageCode;
import choichu.vn.playword.dto.WordDescriptionDTO;
import choichu.vn.playword.dto.WordLinkResponseDTO;
import choichu.vn.playword.utils.CoreStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WordLinkService {

  private final DictionaryService dictionaryService;

  public WordLinkService(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  /**
   * Word link init.
   *
   * @return a random word.
   */
  public ResponseEntity<?> init() {
    WordDescriptionDTO wordDescription = dictionaryService.findARandomWord(2);
    return ResponseEntity.ok(wordDescription);
  }

  /**
   * Answer, check exist answer and get next word.
   *
   * @return a random word.
   */
  public ResponseEntity<?> answer(String word) {
    WordLinkResponseDTO wordLinkResponse = new WordLinkResponseDTO();

    WordDescriptionDTO wordChecked = dictionaryService.findAWord(word);
    if (wordChecked == null) {
      wordLinkResponse.setIsSuccessful(false);
      return ResponseEntity.ok(wordLinkResponse);
    }

    WordDescriptionDTO wordResponse =
        dictionaryService.findARandomWordByStart(CoreStringUtils.getLastWord(word));


    wordLinkResponse.setIsSuccessful(true);
    if (wordResponse == null) {
      wordLinkResponse.setIsFinished(true);
    } else {
      wordLinkResponse.setWordDescription(wordResponse);
    }

    return ResponseEntity.ok(wordLinkResponse);
  }
}
