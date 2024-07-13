package choichu.vn.playword.repository;

import choichu.vn.playword.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  @Query(value =
      "SELECT user "
      + "FROM UserEntity user "
      + "WHERE user.code = :userCode "
      + "  AND user.isActive = TRUE")
  UserEntity findByUserCode(String userCode);
}