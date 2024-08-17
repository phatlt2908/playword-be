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
@Table(name = "chat")
@Getter
@Setter
public class ChatEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_id_seq")
  @SequenceGenerator(name = "chat_id_seq", sequenceName = "chat_id_seq",
                     allocationSize = 1)
  private Long id;

  @Column(name = "message", nullable = false)
  private String message;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "sent_at", nullable = false)
  private Date sentAt;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;
}
