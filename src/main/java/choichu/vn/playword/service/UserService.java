package choichu.vn.playword.service;

import choichu.vn.playword.form.user.UserForm;
import choichu.vn.playword.model.UserEntity;
import choichu.vn.playword.repository.UserRepository;
import java.util.Date;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public ResponseEntity<?> save(UserForm form) {

    UserEntity user;

    if (form.getId() != null) {
      user = this.userRepository.findById(form.getId()).orElse(null);
      if (user != null) {
        user.setCode(form.getCode());
        user.setName(form.getName());
        user.setAvatar(form.getAvatar());
        user.setUpdatedDate(new Date());
        user = this.userRepository.save(user);
        return ResponseEntity.ok(user.getId());
      }
    }

    user = new UserEntity();
    user.setCode(form.getCode());
    user.setName(form.getName());
    user.setAvatar(form.getAvatar());
    user.setCreatedDate(new Date());
    user = this.userRepository.save(user);
    return ResponseEntity.ok(user.getId());
  }
}
