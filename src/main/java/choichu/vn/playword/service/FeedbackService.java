package choichu.vn.playword.service;

import choichu.vn.playword.form.feedback.FeedbackForm;
import choichu.vn.playword.model.FeedbackEntity;
import choichu.vn.playword.model.UserEntity;
import choichu.vn.playword.repository.FeedbackRepository;
import choichu.vn.playword.repository.UserRepository;
import java.util.Date;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {

  private final FeedbackRepository feedbackRepository;
  private final UserRepository userRepository;

  public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
    this.feedbackRepository = feedbackRepository;
    this.userRepository = userRepository;
  }

  public ResponseEntity<?> send(FeedbackForm form) {

    // Check user
    if (form.getUserCode() == null || form.getUserCode().isEmpty()) {
      return ResponseEntity.badRequest().body("User code is required");
    }
    UserEntity user = userRepository.findByUserCode(form.getUserCode());
    if (user == null) {
      return ResponseEntity.badRequest().body("User not found");
    }

    // Check content
    if (form.getContent() == null || form.getContent().isEmpty()) {
      return ResponseEntity.badRequest().body("Content is required");
    }

    FeedbackEntity newFeedback = new FeedbackEntity();
    newFeedback.setUserId(user.getId());
    newFeedback.setContent(form.getContent());
    newFeedback.setCreatedDate(new Date());

    this.feedbackRepository.save(newFeedback);

    return ResponseEntity.ok(null);
  }
}
