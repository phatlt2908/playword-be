package choichu.vn.playword.service;

import choichu.vn.playword.constant.MessageCode;
import choichu.vn.playword.dto.WordDescriptionDTO;
import choichu.vn.playword.model.ViDictionaryEntity;
import choichu.vn.playword.repository.ViDictionaryRepository;
import java.util.List;
import java.util.Random;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

public class DictionaryService {

  private final ViDictionaryRepository viDictionaryRepository;

  public DictionaryService(ViDictionaryRepository viDictionaryRepository) {
    this.viDictionaryRepository = viDictionaryRepository;
  }

  /**
   * Find a random word.
   * @return a random word.
   */
  public ResponseEntity<?> findARandomWord(int wordCount) {
    List<ViDictionaryEntity> wordList =
        viDictionaryRepository.findTop100Used(wordCount, PageRequest.of(0, 100));

    if (CollectionUtils.isEmpty(wordList)) {
      return new ResponseEntity<>(MessageCode.WORD_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    ViDictionaryEntity word = wordList.get(new Random().nextInt(wordList.size()));
    WordDescriptionDTO result = new WordDescriptionDTO(word.getWord(), word.getDescription());

    return ResponseEntity.ok(result);
  }
}
