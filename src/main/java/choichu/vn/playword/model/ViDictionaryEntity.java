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
@Table(name = "vi_dictionary")
@Getter
@Setter
public class ViDictionaryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vi_dictionary_id_seq")
  @SequenceGenerator(name = "vi_dictionary_id_seq", sequenceName = "vi_dictionary_id_seq",
                     allocationSize = 1)
  private Long id;

  @Column(name = "word", nullable = false, unique = true)
  private String word;

  @Column(name = "description")
  private String description;

  @Column(name = "word_count", nullable = false)
  private Integer wordCount;

  @Column(name = "used_count", nullable = false)
  private Integer usedCount;

  @Column(name = "created_date", nullable = false)
  private Date createdDate;

  @Column(name = "created_user")
  private String createdUser;

  @Column(name = "is_approved", nullable = false)
  private Boolean isApproved;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;
}
