package choichu.vn.playword.dto;

import choichu.vn.playword.constant.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MultiModeBaseResponseDTO {
  private MessageType type;
  private Boolean isAnswerCorrect = false;
  private String message;
  private SenderDTO user;
  private RoomDTO room;
}
