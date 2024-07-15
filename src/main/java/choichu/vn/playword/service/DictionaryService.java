package choichu.vn.playword.service;

import choichu.vn.playword.constant.CommonStringConstant;
import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import choichu.vn.playword.model.ViDictionaryEntity;
import choichu.vn.playword.repository.ViDictionaryRepository;
import choichu.vn.playword.utils.CoreStringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DictionaryService {

  private final ViDictionaryRepository viDictionaryRepository;

  public DictionaryService(ViDictionaryRepository viDictionaryRepository) {
    this.viDictionaryRepository = viDictionaryRepository;
  }

  /**
   * Find a random word.
   *
   * @return a random word.
   */
  public WordDescriptionDTO findARandomWordLink() {
    List<ViDictionaryEntity> wordList =
        viDictionaryRepository.findTopUsed(2, true, PageRequest.of(0, 5000));

    if (CollectionUtils.isEmpty(wordList)) {
      return null;
    }

    ViDictionaryEntity word = wordList.get(new Random().nextInt(wordList.size()));

    return new WordDescriptionDTO(word.getWord(), word.getDescription());
  }

  /**
   * Find a word.
   * @param word word to find.
   * @param isForWordLink is for word link.
   * @return word and description.
   */
  public WordDescriptionDTO findAWord(String word, boolean isForWordLink) {
    word = CoreStringUtils.removeExtraSpaces(word);

    List<ViDictionaryEntity> wordResults = viDictionaryRepository.findWord(
        word, false, false, isForWordLink, PageRequest.of(0, 1));

    if (CollectionUtils.isEmpty(wordResults)) {
      return null;
    }

    ViDictionaryEntity wordResult = wordResults.get(0);

    WordDescriptionDTO result = new WordDescriptionDTO(
        wordResult.getWord(), wordResult.getDescription());
    return result;
  }

  /**
   * Find a random word by start.
   * @param startWord start word.
   * @return word and description.
   */
  public WordDescriptionDTO findARandomWordLinkByStart(String startWord,
                                                       List<String> answeredList) {
    startWord = CoreStringUtils.removeExtraSpaces(startWord) + CommonStringConstant.SPACE;

    List<ViDictionaryEntity> wordList = viDictionaryRepository.findTopUsedByStart(
        startWord, 2, true,
        answeredList == null ? new ArrayList<>() : answeredList,
        PageRequest.of(0, 100));

    if (CollectionUtils.isEmpty(wordList)) {
      return null;
    }

    ViDictionaryEntity word = wordList.get(new Random().nextInt(wordList.size()));

    return new WordDescriptionDTO(word.getWord(), word.getDescription());
  }

  public void increaseUsedCount(String word) {
    List<ViDictionaryEntity> wordResults = viDictionaryRepository.findWord(
        word, false, false, false, PageRequest.of(0, 1));

    if (CollectionUtils.isEmpty(wordResults)) {
      return;
    }

    ViDictionaryEntity wordResult = wordResults.get(0);

    wordResult.setUsedCount(wordResult.getUsedCount() + 1);
    viDictionaryRepository.save(wordResult);
  }
}
