package choichu.vn.playword.service;

import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.constant.RoomStatus;
import choichu.vn.playword.dto.RoomDTO;
import choichu.vn.playword.dto.UserDTO;
import choichu.vn.playword.form.multiwordlink.MessageForm;
import choichu.vn.playword.model.RoomEntity;
import choichu.vn.playword.model.UserEntity;
import choichu.vn.playword.repository.RoomRepository;
import choichu.vn.playword.repository.UserRepository;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RoomService {

  private final RedisTemplate<String, RoomDTO> redisTemplate;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;

  public RoomService(RedisTemplate<String, RoomDTO> redisTemplate,
                     RoomRepository roomRepository,
                     UserRepository userRepository) {
    this.redisTemplate = redisTemplate;
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
  }

  public ResponseEntity<Integer> getRoomGame(String id) {
    Optional<RoomEntity> optional = this.roomRepository.findById(id);
    return optional.map(roomEntity -> ResponseEntity.ok(roomEntity.getGame()))
                   .orElseGet(() -> ResponseEntity.notFound().build());
  }

  public void saveRoomToRedis(RoomDTO room) {
    redisTemplate.opsForValue().set(room.getId(), room);
  }

  public void updateRoomToDB(String id, boolean isIncreaseRound) {
    Optional<RoomEntity> optional = this.roomRepository.findById(id);
    if (optional.isPresent()) {
      RoomEntity roomEntity = optional.get();
      roomEntity.setIsActive(true);
      roomEntity.setRound(isIncreaseRound ? roomEntity.getRound() + 1 : roomEntity.getRound());
      this.roomRepository.save(roomEntity);
    }
  }

  public void createAnEmptyRoom(String roomId, String roomName, String userCode, int game) {
    RoomDTO room = new RoomDTO();
    room.setId(roomId);
    room.setName(roomName);
    room.setGame(game);
    room.setStatus(RoomStatus.PREPARING);
    this.saveRoomToRedis(room);
    this.createRoomToDB(roomId, roomName, userCode, game);
  }

  public void createRoomToDB(String id, String name, String userCode, int game) {
    Optional<RoomEntity> optional = this.roomRepository.findById(id);
    RoomEntity roomEntity = optional.orElseGet(RoomEntity::new);

    UserEntity user = userRepository.findByUserCode(userCode);
    roomEntity.setId(id);
    roomEntity.setName(name);
    roomEntity.setCreatedDate(new Date());
    roomEntity.setCreatedBy(user.getId());
    roomEntity.setFinishedAt(null);
    roomEntity.setIsActive(true);
    roomEntity.setRound(0);
    roomEntity.setGame(game);
    this.roomRepository.save(roomEntity);
  }

  public RoomDTO findRoomById(String id) {
    return redisTemplate.opsForValue().get(id);
  }

  public RoomDTO addUserToRoom(MessageForm messageForm) {
    RoomDTO room = this.findRoomById(messageForm.getRoomId());
    if (room == null) {
      room = new RoomDTO();
      room.setId(messageForm.getRoomId());
      room.setName(messageForm.getRoomName());
      room.setStatus(RoomStatus.PREPARING);

      UserDTO user = new UserDTO();
      user.setCode(messageForm.getSender().getCode());
      user.setName(messageForm.getSender().getName());
      user.setAvatar(messageForm.getSender().getAvatar());
      user.setOrder(1);
      user.setIsReady(false);
      user.setIsKey(true);

      room.getUserList().add(user);

    }
    else {
      if (RoomStatus.STARTED.equals(room.getStatus())) {
        log.error("Room is started. RoomId: {}", messageForm.getRoomId());
        return null;
      }

      if (room.getUserList().size() >= 2
          && CommonConstant.SOLO_ROOM_NAME.equals(room.getName())) {
        log.error("Room is full. RoomId: {}", messageForm.getRoomId());
        return null;
      }

      // Return null if userList has the same user
      if (room.getUserList().stream()
              .anyMatch(u -> u.getCode().equals(messageForm.getSender().getCode()))) {
        log.error("User is already in the room. UserCode: {}", messageForm.getSender().getCode());
        return null;
      }

      UserDTO user = new UserDTO();
      user.setCode(messageForm.getSender().getCode());
      user.setName(messageForm.getSender().getName());
      user.setAvatar(messageForm.getSender().getAvatar());
      user.setIsReady(false);

      int maxOrder = room.getUserList().stream()
                         .mapToInt(UserDTO::getOrder) // Extract only the value
                         .max()
                         .orElse(-1);
      user.setOrder(maxOrder + 1);

      if (room.getUserList().stream().noneMatch(UserDTO::getIsKey)) {
        user.setIsKey(true);
      }

      room.getUserList().add(user);
    }

    this.saveRoomToRedis(room);
    this.updateRoomToDB(room.getId(), false);

    return room;
  }

  public void deleteRoom(String roomId) {
    redisTemplate.delete(roomId);
    this.deactivateRoom(roomId);
    log.info("Room {} is deleted", roomId);
  }

  private void deactivateRoom(String id) {
    this.roomRepository.findById(id)
                       .ifPresent(roomEntity -> {
                         roomEntity.setIsActive(false);
                         roomEntity.setFinishedAt(new Date());
                         this.roomRepository.save(roomEntity);
                       });
  }

  public void resetRoom(RoomDTO room) {
    room.setStatus(RoomStatus.PREPARING);
    room.getUserList().forEach(u -> {
      u.setIsReady(false);
      u.setIsAnswering(false);
    });
    room.getWordList().clear();
  }
}
