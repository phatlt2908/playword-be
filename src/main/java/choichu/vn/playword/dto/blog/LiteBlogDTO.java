package choichu.vn.playword.dto.blog;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LiteBlogDTO {
  private String code;
  private String title;
  private String description;
  private String image;
  private Date createdDate;
  private Long readNum;
}
