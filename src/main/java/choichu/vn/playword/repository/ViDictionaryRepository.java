package choichu.vn.playword.repository;

import choichu.vn.playword.model.ViDictionaryEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ViDictionaryRepository extends JpaRepository<ViDictionaryEntity, Long> {
  @Query(value = "SELECT * FROM ViDictionaryEntity dic "
                 + "WHERE :wordCount = 0 OR dic.wordCount = :wordCount "
                 + "ORDER BY dic.usedCount DESC")
  List<ViDictionaryEntity> findTop100Used(int wordCount, Pageable pageable);
}