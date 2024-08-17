package choichu.vn.playword.repository;

import choichu.vn.playword.model.ChatEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

  @Query(value =
      "SELECT c FROM ChatEntity c "
      + "WHERE :largestId = 0 OR c.id < :largestId "
      + "ORDER BY c.id DESC")
  List<ChatEntity> getByIdSmallerThan(Long largestId, Pageable pageable);
}