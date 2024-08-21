package choichu.vn.playword.repository;

import choichu.vn.playword.model.ViDictionaryEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ViDictionaryRepository extends JpaRepository<ViDictionaryEntity, Long> {

  @Query(value = "SELECT dic FROM ViDictionaryEntity dic "
                 + "WHERE (:wordCount = 0 OR dic.wordCount = :wordCount) "
                 + "  AND dic.isApproved = TRUE "
                 + "  AND dic.isDeleted = FALSE "
                 + "  AND (:isForWordLink = FALSE OR dic.isWordLink = TRUE) "
                 + "  AND dic.usedCount > 0")
  List<ViDictionaryEntity> findAllWordLinkable(int wordCount, boolean isForWordLink);

  @Query(value = "SELECT dic FROM ViDictionaryEntity dic "
                 + "WHERE dic.isApproved = TRUE "
                 + "  AND dic.isDeleted = FALSE "
                 + "  AND ((dic.wordCount = 2 AND dic.isWordLink = TRUE AND dic.usedCount > 0) "
                 + "    OR dic.wordCount = 3) ")
  List<ViDictionaryEntity> findAllStickWord();

  @Query(value = "SELECT dic FROM ViDictionaryEntity dic "
                 + "WHERE (:wordCount = 0 OR dic.wordCount = :wordCount) "
                 + "  AND LOWER(dic.word) LIKE CONCAT(LOWER(:startWord), '%') "
                 + "  AND dic.isApproved = TRUE "
                 + "  AND dic.isDeleted = FALSE "
                 + "  AND (:isForWordLink = FALSE OR dic.isWordLink = TRUE) "
                 + "  AND LOWER(dic.word) NOT IN :excepts "
                 + "ORDER BY dic.usedCount DESC")
  List<ViDictionaryEntity> findTopUsedByStart(
      String startWord, int wordCount, boolean isForWordLink, List<String> excepts,
      Pageable pageable);

  @Query(value = "SELECT dic FROM ViDictionaryEntity dic "
                 + "WHERE LOWER(dic.word) = LOWER(:word) "
                 + "  AND (:isIncludeNotApproved = TRUE OR dic.isApproved = TRUE) "
                 + "  AND (:isIncludeDeleted = TRUE OR dic.isDeleted = FALSE) "
                 + "  AND (:isForWordLink = FALSE OR dic.isWordLink = TRUE) "
                 + "ORDER BY dic.usedCount DESC")
  List<ViDictionaryEntity> findWord(
      String word, boolean isIncludeNotApproved, boolean isIncludeDeleted, boolean isForWordLink,
      Pageable pageable);

  @Query(value = "SELECT dic FROM ViDictionaryEntity dic "
                 + "WHERE LOWER(dic.word) IN :wordList "
                 + "  AND dic.isApproved = TRUE "
                 + "  AND dic.isDeleted = FALSE")
  List<ViDictionaryEntity> getWordByList(List<String> wordList, Pageable pageable);
}