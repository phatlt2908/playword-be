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
@Table(name = "report")
@Getter
@Setter
public class ReportEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_id_seq")
  @SequenceGenerator(name = "report_id_seq", sequenceName = "report_id_seq",
                     allocationSize = 1)
  private Long id;

  @Column(name = "word", nullable = false, unique = true)
  private String word;

  @Column(name = "issue_type", nullable = false)
  private Integer issueType;

  @Column(name = "created_user_id")
  private Long createdUserId;

  @Column(name = "created_date", nullable = false)
  private Date createdDate;

  @Column(name = "is_resolved", nullable = false)
  private Boolean isResolved = false;

  @Column(name = "is_rejected", nullable = false)
  private Boolean isRejected = false;

  @Column(name = "note")
  private String note;
}
