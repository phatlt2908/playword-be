package choichu.vn.playword.service;

import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import choichu.vn.playword.dto.dictionary.WordLinkResponseDTO;
import choichu.vn.playword.utils.CoreStringUtils;
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
    WordDescriptionDTO wordDescription = dictionaryService.findARandomWordLink();
    return ResponseEntity.ok(wordDescription);
  }

  /**
   * Answer, check exist answer and get next word.
   *
   * @return a random word.
   */
  public ResponseEntity<?> answer(String word) {
    WordLinkResponseDTO wordLinkResponse = new WordLinkResponseDTO();

    WordDescriptionDTO wordChecked = dictionaryService.findAWord(word, false);
    if (wordChecked == null) {
      wordLinkResponse.setIsSuccessful(false);
      return ResponseEntity.ok(wordLinkResponse);
    }

    // Create a new thread to register the word to the database.
    new Thread(() -> dictionaryService.increaseUsedCount(word)).start();

    WordDescriptionDTO wordResponse =
        dictionaryService.findARandomWordLinkByStart(CoreStringUtils.getLastWord(word));

    wordLinkResponse.setIsSuccessful(true);
    if (wordResponse == null) {
      wordLinkResponse.setIsFinished(true);
    } else {
      wordLinkResponse.setWordDescription(wordResponse);
    }

    return ResponseEntity.ok(wordLinkResponse);
  }
}
