package choichu.vn.playword.dto.stick;

import choichu.vn.playword.dto.MultiModeBaseResponseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MultiModeStickResponseDTO extends MultiModeBaseResponseDTO {
  private StickWordDTO stickWord;
}
