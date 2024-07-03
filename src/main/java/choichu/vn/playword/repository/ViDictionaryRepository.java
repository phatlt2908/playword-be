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
                 + "ORDER BY dic.usedCount DESC")
  List<ViDictionaryEntity> findTopUsed(int wordCount, boolean isForWordLink, Pageable pageable);

  @Query(value = "SELECT dic FROM ViDictionaryEntity dic "
                 + "WHERE (:wordCount = 0 OR dic.wordCount = :wordCount) "
                 + "  AND dic.word LIKE CONCAT(:startWord, '%') "
                 + "  AND dic.isApproved = TRUE "
                 + "  AND dic.isDeleted = FALSE "
                 + "  AND (:isForWordLink = FALSE OR dic.isWordLink = TRUE) "
                 + "  AND COALESCE(:excepts, NULL) IS NULL OR dic.word NOT IN :excepts "
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
}