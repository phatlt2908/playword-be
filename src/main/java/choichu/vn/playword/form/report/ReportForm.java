package choichu.vn.playword.form.report;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportForm {
  private String word;

  // 0: N/A, 1: Miss, 2: No meaning
  private Integer issueType;
}
