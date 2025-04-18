package choichu.vn.playword.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseWordResponseDTO {
  private WordDescriptionDTO wordDescription = new WordDescriptionDTO();
  private Boolean isSuccessful = false;
  private Boolean isFinished = false;
}
