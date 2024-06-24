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

  public RoomEntity(String id, String name) {
    this.id = id;
    this.name = name;
    this.isActive = true;
    this.createdDate = new Date();
  }
}
