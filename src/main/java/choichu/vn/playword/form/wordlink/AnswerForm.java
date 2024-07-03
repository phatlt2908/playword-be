package choichu.vn.playword.form.wordlink;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AnswerForm {

  private String answer;
  private List<String> answeredList = new ArrayList<>();
}
