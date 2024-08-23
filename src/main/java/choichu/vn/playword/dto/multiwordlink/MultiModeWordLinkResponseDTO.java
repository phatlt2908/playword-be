package choichu.vn.playword.dto.multiwordlink;

import choichu.vn.playword.dto.MultiModeBaseResponseDTO;
import choichu.vn.playword.dto.dictionary.WordDescriptionDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MultiModeWordLinkResponseDTO extends MultiModeBaseResponseDTO {
  private WordDescriptionDTO word;
}
