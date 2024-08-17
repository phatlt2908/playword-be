package choichu.vn.playword.form.chat;

import choichu.vn.playword.constant.ChatType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatForm {

  private ChatType type;
  private String userCode;
  private String message;
}
