package choichu.vn.playword.dto.multiwordlink;

import choichu.vn.playword.constant.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageDTO {
  private MessageType type;
  private String message;
  private SenderDTO user;
  private RoomDTO room;
}
