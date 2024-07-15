package choichu.vn.playword.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RankingChartDTO {
  private String userCode;
  private String userName;
  private String avatar;
  private Integer point;
  private Integer rank;

  public RankingChartDTO(String userCode, String userName, String avatar, Integer point) {
    this.userCode = userCode;
    this.userName = userName;
    this.avatar = avatar;
    this.point = point;
  }
}
