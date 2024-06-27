package choichu.vn.playword.repository;

import choichu.vn.playword.model.SingleRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SingleRoomRepository extends JpaRepository<SingleRoomEntity, Long> {

  @Query(value = "SELECT COUNT(*) FROM SingleRoomEntity WHERE point > :point")
  Long getRank(int point);
}