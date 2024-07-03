package choichu.vn.playword.service;

import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import choichu.vn.playword.dto.dictionary.WordLinkResponseDTO;
import choichu.vn.playword.form.wordlink.AnswerForm;
import choichu.vn.playword.model.SingleRoomEntity;
import choichu.vn.playword.repository.SingleRoomRepository;
import choichu.vn.playword.utils.CoreStringUtils;
import java.util.Date;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WordLinkService {

  private final DictionaryService dictionaryService;
  private final SingleRoomRepository singleRoomRepository;

  public WordLinkService(DictionaryService dictionaryService,
                         SingleRoomRepository singleRoomRepository) {
    this.dictionaryService = dictionaryService;
    this.singleRoomRepository = singleRoomRepository;
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
  public ResponseEntity<?> answer(AnswerForm form) {
    WordLinkResponseDTO wordLinkResponse = new WordLinkResponseDTO();

    WordDescriptionDTO wordChecked = dictionaryService.findAWord(form.getAnswer(), true);
    if (wordChecked == null) {
      wordLinkResponse.setIsSuccessful(false);
      return ResponseEntity.ok(wordLinkResponse);
    }

    // Create a new thread to register the word to the database.
    new Thread(() -> dictionaryService.increaseUsedCount(form.getAnswer())).start();

    WordDescriptionDTO wordResponse =
        dictionaryService.findARandomWordLinkByStart(
            CoreStringUtils.getLastWord(form.getAnswer()), form.getAnsweredList());

    wordLinkResponse.setIsSuccessful(true);
    if (wordResponse == null) {
      wordLinkResponse.setIsFinished(true);
    } else {
      wordLinkResponse.setWordDescription(wordResponse);
    }

    return ResponseEntity.ok(wordLinkResponse);
  }

  public ResponseEntity<?> getRank(Integer point) {
    if (point == null || point <= 0) {
      return ResponseEntity.ok(0);
    }

    SingleRoomEntity singleRoom = new SingleRoomEntity();
    singleRoom.setCreatedDate(new Date());
    singleRoom.setPoint(point);
    singleRoomRepository.save(singleRoom);

    Long rank = singleRoomRepository.getRank(point);
    rank++;

    return ResponseEntity.ok(rank);
  }
}
