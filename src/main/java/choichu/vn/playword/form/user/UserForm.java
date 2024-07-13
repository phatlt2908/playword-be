package choichu.vn.playword.form.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserForm {
  private Long id;
  private String code;
  private String name;
  private String avatar;
}
