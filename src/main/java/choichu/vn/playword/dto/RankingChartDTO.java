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
}
