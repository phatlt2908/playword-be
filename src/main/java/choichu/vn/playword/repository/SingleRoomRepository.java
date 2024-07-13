package choichu.vn.playword.repository;

import choichu.vn.playword.dto.RankingChartDTO;
import choichu.vn.playword.model.SingleRoomEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SingleRoomRepository extends JpaRepository<SingleRoomEntity, Long> {

  @Query(value = "SELECT COUNT(*) FROM SingleRoomEntity WHERE point > :point")
  Long getRank(int point);

  // getRankingChart
  @Query(value =
             "SELECT new choichu.vn.playword.dto.RankingChartDTO("
             + "  u.code, u.name, u.avatar, MAX(sr.point) AS highest_point)"
             + "FROM UserEntity u "
             + "INNER JOIN SingleRoomEntity sr ON sr.userId = u.id "
             + "GROUP BY u.code, u.name, u.avatar "
             + "ORDER BY highest_point DESC")
  List<RankingChartDTO> getRankingChart(Pageable pageable);
}