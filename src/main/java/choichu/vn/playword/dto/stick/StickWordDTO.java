package choichu.vn.playword.dto.stick;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StickWordDTO {
  private String[] characters;
  private String wordBase64Encoded;
  private String descriptionBase64Encoded;
}
