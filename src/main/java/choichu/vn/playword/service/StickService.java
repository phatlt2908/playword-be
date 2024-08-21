package choichu.vn.playword.service;

import choichu.vn.playword.dto.RankingChartDTO;
import choichu.vn.playword.dto.dictionary.BaseWordResponseDTO;
import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import choichu.vn.playword.dto.stick.StickWordDTO;
import choichu.vn.playword.model.SingleRoomEntity;
import choichu.vn.playword.model.UserEntity;
import choichu.vn.playword.model.ViDictionaryEntity;
import choichu.vn.playword.repository.SingleRoomRepository;
import choichu.vn.playword.repository.UserRepository;
import choichu.vn.playword.repository.ViDictionaryRepository;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class StickService {

  private final ViDictionaryRepository viDictionaryRepository;
  private final UserRepository userRepository;
  private final SingleRoomRepository singleRoomRepository;

  public StickService(ViDictionaryRepository viDictionaryRepository,
                      UserRepository userRepository,
                      SingleRoomRepository singleRoomRepository) {
    this.viDictionaryRepository = viDictionaryRepository;
    this.userRepository = userRepository;
    this.singleRoomRepository = singleRoomRepository;
  }

  public ResponseEntity<?> getWord() {

    // Get a random word to start
    List<ViDictionaryEntity> wordList = viDictionaryRepository.findAllStickWord();

    if (CollectionUtils.isEmpty(wordList)) {
      log.error("Can not get a random word to start");
      return null;
    }
    ViDictionaryEntity word = wordList.get(new Random().nextInt(wordList.size()));

    // Split this word into separate characters (remove spaces)
    String[] characters = word.getWord().replaceAll("\\s+", "").split("");

    // Uppercase the first character
    characters[0] = characters[0].toUpperCase();

    // Random shuffle the characters in the string
    for (int i = 0; i < characters.length; i++) {
      int randomIndex = new Random().nextInt(characters.length);
      String temp = characters[i];
      characters[i] = characters[randomIndex];
      characters[randomIndex] = temp;
    }

    StickWordDTO stickWord = new StickWordDTO();
    stickWord.setCharacters(characters);
    stickWord.setWordBase64Encoded(Base64.getEncoder().encodeToString(word.getWord().getBytes()));
    stickWord.setDescriptionBase64Encoded(
        Base64.getEncoder().encodeToString(word.getDescription().getBytes(StandardCharsets.UTF_8)));

    return ResponseEntity.ok(stickWord);
  }

  public ResponseEntity<?> answer(String answer) {
    BaseWordResponseDTO response = new BaseWordResponseDTO();

    List<String> answerList = new ArrayList<>();

    // Lowercase the answer
    answer = answer.toLowerCase();

    for (int i = 1; i <= answer.length(); i++) {
      String modifiedString = answer.substring(0, i) + " " + answer.substring(i);
      answerList.add(modifiedString);
    }

    WordDescriptionDTO wordChecked =
        viDictionaryRepository.getWordByList(answerList,
                                             PageRequest.of(0, 1))
                              .stream()
                              .findFirst()
                              .map(w -> new WordDescriptionDTO(
                                  w.getWord(), w.getDescription()))
                              .orElse(null);

    if (wordChecked == null) {
      return ResponseEntity.ok(response);
    }

    response.setIsSuccessful(true);
    response.setWordDescription(wordChecked);
    return ResponseEntity.ok(response);
  }

  public ResponseEntity<?> getRank(Integer point, String userCode) {
    if (point == null || point <= 0) {
      return ResponseEntity.ok(0);
    }

    UserEntity user = userRepository.findByUserCode(userCode);

    SingleRoomEntity singleRoom = new SingleRoomEntity();
    singleRoom.setCreatedDate(new Date());
    singleRoom.setPoint(point);
    singleRoom.setUserId(user.getId());
    singleRoom.setGame(2);
    singleRoomRepository.save(singleRoom);

    Integer rank = singleRoomRepository.getRank(point, 2);
    rank++;

    return ResponseEntity.ok(rank);
  }

  public ResponseEntity<?> getRankingChart(int top) {
    List<RankingChartDTO> rankingChartList =
        singleRoomRepository.getRankingChart(2, PageRequest.of(0, top));

    for (int i = 0; i < rankingChartList.size(); i++) {
      if (i > 0 && rankingChartList.get(i).getPoint().equals(rankingChartList.get(i - 1).getPoint())) {
        rankingChartList.get(i).setRank(rankingChartList.get(i - 1).getRank());
        continue;
      }
      rankingChartList.get(i).setRank(i + 1);
    }

    return ResponseEntity.ok(rankingChartList);
  }

  public ResponseEntity<?> getUserRanking(String userCode) {

    RankingChartDTO userRanking = singleRoomRepository.getRankingChartByUserCode(2, userCode);

    if (userRanking == null) {
      return ResponseEntity.ok(null);
    }

    Integer rank = singleRoomRepository.getRank(userRanking.getPoint(), 1);
    userRanking.setRank(rank + 1);

    return ResponseEntity.ok(userRanking);
  }
}
