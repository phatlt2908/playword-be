package choichu.vn.playword.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "room")
@Getter
@Setter
public class RoomEntity {

  @Id
  private String id;

  @Column(name = "name")
  private String name;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "created_date", nullable = false)
  private Date createdDate;

  @Column(name = "created_by")
  private Long createdBy;

  @Column(name = "finished_at")
  private Date finishedAt;

  @Column(name = "round")
  private Integer round = 0;

  @Column(name = "game")
  private Integer game = 1;
}
