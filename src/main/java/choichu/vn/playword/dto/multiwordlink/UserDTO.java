package choichu.vn.playword.dto.multiwordlink;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private String code;
  private String name;
  private String avatar;
  private Integer order;
  private Boolean isReady = false;
  private Boolean isAnswering = false;
}
