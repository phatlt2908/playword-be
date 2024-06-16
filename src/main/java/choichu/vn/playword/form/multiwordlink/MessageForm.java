package choichu.vn.playword.form.multiwordlink;

import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.dto.multiwordlink.SenderDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageForm {
  private MessageType type;
  private String roomId;
  private String roomName;
  private String message;
  private SenderDTO sender;
}
