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
@Table(name = "single_room")
@Getter
@Setter
public class SingleRoomEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "single_room_id_seq")
  @SequenceGenerator(name = "single_room_id_seq", sequenceName = "single_room_id_seq",
                     allocationSize = 1)
  private Long id;

  @Column(name = "created_date", nullable = false)
  private Date createdDate;

  @Column(name = "point", nullable = false)
  private Integer point = 0;
}
