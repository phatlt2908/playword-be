package choichu.vn.playword.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blog")
@Getter
@Setter
public class BlogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blog_id_seq")
  @SequenceGenerator(name = "blog_id_seq", sequenceName = "blog_id_seq",
                     allocationSize = 1)
  private Long id;

  @Column(name = "code")
  private String code;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "image")
  private String image;

  @Column(name = "read_num", nullable = false)
  private Long readNum = 0L;

  @Column(name = "created_date", nullable = false)
  private Date createdDate;

  @Column(name = "updated_date")
  private Date updatedDate;
}
