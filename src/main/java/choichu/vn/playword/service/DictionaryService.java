package choichu.vn.playword.service;

import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import choichu.vn.playword.model.ViDictionaryEntity;
import choichu.vn.playword.repository.ViDictionaryRepository;
import choichu.vn.playword.utils.CoreStringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DictionaryService {

  private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);
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
    ViDictionaryEntity word;
    WordDescriptionDTO continueWord;
    do {
      List<ViDictionaryEntity> wordList =
          viDictionaryRepository.findAllWordLinkable(2, true);
      if (CollectionUtils.isEmpty(wordList)) {
        log.error("Can not get a random word to start");
        return null;
      }

      word = wordList.get(new Random().nextInt(wordList.size()));
      // Check if the above random word has a link to another word. If not, find another word.
      // Because the game must be continued after init.
      continueWord = findARandomWordLinkByStart(
          CoreStringUtils.getLastWord(word.getWord()), null);
    } while (Objects.isNull(continueWord));

    return new WordDescriptionDTO(word.getWord(), word.getDescription());
  }

  /**
   * Find a word.
   *
   * @param word          word to find.
   * @param isForWordLink is for word link.
   * @return word and description.
   */
  public WordDescriptionDTO findAWord(String word, boolean isForWordLink) {
    word = CoreStringUtils.removeExtraSpaces(word);

    List<ViDictionaryEntity> wordResults = viDictionaryRepository.findWord(
        word, false, false, isForWordLink, PageRequest.of(0, 1));

    if (CollectionUtils.isEmpty(wordResults)) {
      String newTypeWord = replaceOldTypeCharactersWithNewTypeCharacters(word);
      wordResults = viDictionaryRepository.findWord(
          newTypeWord, false, false, isForWordLink, PageRequest.of(0, 1));
    }

    if (CollectionUtils.isEmpty(wordResults)) {
      String oldTypeWord = replaceNewTypeCharactersWithOldTypeCharacters(word);
      wordResults = viDictionaryRepository.findWord(
          oldTypeWord, false, false, isForWordLink, PageRequest.of(0, 1));
    }

    if (CollectionUtils.isEmpty(wordResults)) {
      return null;
    }

    ViDictionaryEntity wordResult = wordResults.getFirst();

    return new WordDescriptionDTO(
        wordResult.getWord(), wordResult.getDescription());
  }

  /**
   * Find a random word by start.
   *
   * @param startWord start word.
   * @return word and description.
   */
  public WordDescriptionDTO findARandomWordLinkByStart(String startWord,
                                                       List<String> answeredList) {
    startWord = CoreStringUtils.removeExtraSpaces(startWord) + CommonConstant.SPACE;

    List<ViDictionaryEntity> wordList = viDictionaryRepository.findTopUsedByStart(
        startWord, 2, true,
        answeredList == null ? List.of("") : answeredList,
        PageRequest.of(0, 100));

    if (CollectionUtils.isEmpty(wordList)) {
      String newTypeStartWord = replaceOldTypeCharactersWithNewTypeCharacters(startWord);
      wordList = viDictionaryRepository.findTopUsedByStart(
          newTypeStartWord, 2, true,
          answeredList == null ? new ArrayList<>() : answeredList,
          PageRequest.of(0, 100));
    }

    if (CollectionUtils.isEmpty(wordList)) {
      String oldTypeStartWord = replaceNewTypeCharactersWithOldTypeCharacters(startWord);
      wordList = viDictionaryRepository.findTopUsedByStart(
          oldTypeStartWord, 2, true,
          answeredList == null ? new ArrayList<>() : answeredList,
          PageRequest.of(0, 100));
    }

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

  private String replaceOldTypeCharactersWithNewTypeCharacters(String word) {
    return word.replace("òa", "oà").replace("óa", "oá")
               .replace("ỏa", "oả").replace("õa", "oã")
               .replace("ọa", "oạ").replace("òe", "oè")
               .replace("óe", "oé").replace("ỏe", "oẻ")
               .replace("õe", "oẽ").replace("ọe", "oẹ")
               .replace("ùy", "uỳ").replace("úy", "uý")
               .replace("ủy", "uỷ").replace("ũy", "uỹ")
               .replace("ụy", "uỵ");
  }

  private String replaceNewTypeCharactersWithOldTypeCharacters(String word) {
    return word.replace("oà", "òa").replace("oá", "óa")
               .replace("oả", "ỏa").replace("oã", "õa")
               .replace("oạ", "ọa").replace("oè", "òe")
               .replace("oé", "óe").replace("oẻ", "ỏe")
               .replace("oẽ", "õe").replace("oẹ", "ọe")
               .replace("uỳ", "ùy").replace("uý", "úy")
               .replace("uỷ", "ủy").replace("uỹ", "ũy")
               .replace("uỵ", "ụy");
  }
}
