package choichu.vn.playword.dto.chat;

import choichu.vn.playword.constant.ChatType;
import choichu.vn.playword.dto.multiwordlink.SenderDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatDTO {
  private ChatType type;
  private SenderDTO user;
  private String message;
  private Integer onlineCount;
}
