package choichu.vn.playword.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseRoomInfoDTO {
  private String id;
  private String name;
  private Integer userCount;
  private String status;
}
