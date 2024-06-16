package choichu.vn.playword.dto.multiwordlink;

import choichu.vn.playword.constant.RoomStatus;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private RoomStatus status;
  private List<UserDTO> userList = new ArrayList<>();
}
