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

  @Query(value =
      "WITH ranked_users AS ("
      + "  SELECT "
      + "    user_id, "
      + "    MAX(point) AS max_point "
      + "  FROM single_room "
      + "  WHERE game = :game "
      + "  GROUP BY user_id "
      + ")"
      + ""
      + "SELECT "
      + "  COUNT(*) "
      + "FROM ranked_users "
      + "WHERE max_point > :point", nativeQuery = true)
  Integer getRank(int point, int game);

  // getRankingChart
  @Query(value =
      "SELECT new choichu.vn.playword.dto.RankingChartDTO("
      + " u.code, u.name, u.avatar, MAX(sr.point) AS highest_point)"
      + "FROM UserEntity u "
      + "INNER JOIN SingleRoomEntity sr ON sr.userId = u.id "
      + "WHERE sr.game = :game "
      + "GROUP BY u.code, u.name, u.avatar "
      + "ORDER BY highest_point DESC")
  List<RankingChartDTO> getRankingChart(int game, Pageable pageable);

  @Query(value =
      "SELECT new choichu.vn.playword.dto.RankingChartDTO("
      + " u.code, u.name, u.avatar, MAX(sr.point)) "
      + "FROM SingleRoomEntity sr "
      + "INNER JOIN UserEntity u ON u.id = sr.userId "
      + "WHERE u.code = :userCode "
      + " AND sr.game = :game "
      + "GROUP BY u.code, u.name, u.avatar")
  RankingChartDTO getRankingChartByUserCode(int game, String userCode);
}