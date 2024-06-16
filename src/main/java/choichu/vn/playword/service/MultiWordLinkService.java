package choichu.vn.playword.service;

import choichu.vn.playword.constant.RoomStatus;
import choichu.vn.playword.dto.multiwordlink.RoomDTO;
import choichu.vn.playword.dto.multiwordlink.UserDTO;
import choichu.vn.playword.form.multiwordlink.MessageForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MultiWordLinkService {

  @Autowired
  private RedisTemplate<String, RoomDTO> redisTemplate;

  public RoomDTO addUserToRoom(MessageForm messageForm) {
    RoomDTO room = this.findRoomById(messageForm.getRoomId());
    if (room == null) {
      room = new RoomDTO();
      room.setId(messageForm.getRoomId());
      room.setName(messageForm.getRoomName());
      room.setStatus(RoomStatus.PREPARING);

      UserDTO user = new UserDTO();
      user.setId(messageForm.getSender().getId());
      user.setName(messageForm.getSender().getName());
      user.setOrder(1);
      user.setIsReady(false);

      room.getUserList().add(user);

      this.saveRoom(room);
    } else {
      UserDTO user = new UserDTO();
      user.setId(messageForm.getSender().getId());
      user.setName(messageForm.getSender().getName());
      user.setIsReady(false);

      int maxOrder = room.getUserList().stream()
                         .mapToInt(UserDTO::getOrder) // Extract only the value
                         .max()
                         .orElse(-1);
      user.setOrder(maxOrder + 1);

      room.getUserList().add(user);
      this.saveRoom(room);
    }

    return room;
  }

  public RoomDTO readyUser(MessageForm messageForm) {
    RoomDTO room = this.findRoomById(messageForm.getRoomId());
    if (room == null) {
      return null;
    }

    UserDTO user = room.getUserList().stream()
                       .filter(u -> u.getId().equals(messageForm.getSender().getId()))
                       .findFirst()
                       .orElse(null);
    if (user == null) {
      return null;
    }

    user.setIsReady(true);

    if (room.getUserList().stream().allMatch(UserDTO::getIsReady)) {
      room.setStatus(RoomStatus.STARTED);
    }

    this.saveRoom(room);

    return room;
  }

  private void saveRoom(RoomDTO room) {
    redisTemplate.opsForValue().set(room.getId(), room);
  }

  private RoomDTO findRoomById(String id) {
    return redisTemplate.opsForValue().get(id);
  }
}
