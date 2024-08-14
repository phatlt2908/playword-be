package choichu.vn.playword.service;

import choichu.vn.playword.dto.blog.LiteBlogDTO;
import choichu.vn.playword.repository.BlogRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BlogService {

  private final BlogRepository blogRepository;

  public BlogService(BlogRepository blogRepository) {
    this.blogRepository = blogRepository;
  }

  public ResponseEntity<?> getBlogList() {
    List<LiteBlogDTO> blogList = blogRepository.getLiteBlogList(PageRequest.of(0, 10));
    return ResponseEntity.ok(blogList);
  }

  public ResponseEntity<?> getBlogDetail(String code) {
    return ResponseEntity.ok(blogRepository.findByCode(code));
  }
}
