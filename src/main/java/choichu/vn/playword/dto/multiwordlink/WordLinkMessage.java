package choichu.vn.playword.dto.multiwordlink;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WordLinkMessage {
  private MessageType type;
  private String roomId;
  private String message;
  private String sender;

  public enum MessageType {
    ANSWER,
    CHAT,
    JOIN,
    LEAVE
  }
}
