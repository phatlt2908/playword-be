package choichu.vn.playword.dto.chat;

import choichu.vn.playword.dto.multiwordlink.SenderDTO;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryChatDTO {

  private Long id;
  private SenderDTO user;
  private String message;
  private Date sentAt;
}
