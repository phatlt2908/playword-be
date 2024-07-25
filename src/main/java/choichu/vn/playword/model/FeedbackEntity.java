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
@Table(name = "feedback")
@Getter
@Setter
public class FeedbackEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedback_id_seq")
  @SequenceGenerator(name = "feedback_id_seq", sequenceName = "feedback_id_seq",
                     allocationSize = 1)
  private Long id;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "created_date", nullable = false)
  private Date createdDate;

  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false;
}
