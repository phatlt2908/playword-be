package choichu.vn.playword.dto.multiwordlink;

import choichu.vn.playword.constant.MessageType;
import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseDTO {
  private MessageType type;
  private Boolean isAnswerCorrect = false;
  private WordDescriptionDTO word;
  private SenderDTO user;
  private RoomDTO room;
}
