package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.constant.UserApiUrlConstant;
import choichu.vn.playword.form.user.UserForm;
import choichu.vn.playword.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = "*")
@RequestMapping(value = CommonConstant.BASE_API_URL)
@RestController
@Controller
@Slf4j
public class UserController {

  UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping(value = UserApiUrlConstant.SAVE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> save(@RequestBody UserForm form) {
    return this.userService.save(form);
  }
}
