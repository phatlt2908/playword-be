package choichu.vn.playword.repository;

import choichu.vn.playword.model.RoomEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, String> {

  @Query("SELECT r FROM RoomEntity r "
         + "WHERE ((r.name IS NULL OR r.name LIKE CONCAT('%', :keyword, '%')) "
         + "    OR r.id LIKE CONCAT('%', :keyword, '%')) "
         + "  AND r.isActive = true "
         + "  AND (:game IS NULL OR r.game = :game) "
         + "ORDER BY r.createdDate DESC")
  List<RoomEntity> search(Integer game, String keyword, Pageable pageable);
}