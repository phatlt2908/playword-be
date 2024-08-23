package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.constant.RoomApiUrlConstant;
import choichu.vn.playword.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = "*")
@RequestMapping(value = CommonConstant.BASE_API_URL)
@RestController
@Controller
@Slf4j
public class RoomController {

  RoomService roomService;

  public RoomController(RoomService roomService) {
    this.roomService = roomService;
  }

  @GetMapping(value = RoomApiUrlConstant.FIND_ROOM_GAME, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getRoomGame(@RequestParam String id) {
    return this.roomService.getRoomGame(id);
  }
}
