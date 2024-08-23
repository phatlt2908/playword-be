package choichu.vn.playword.controller;

import choichu.vn.playword.constant.BlogApiUrlConstant;
import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = "*")
@RequestMapping(value = CommonConstant.BASE_API_URL)
@RestController
@Slf4j
public class BlogController {

  private final BlogService blogService;

  public BlogController(BlogService blogService) {
    this.blogService = blogService;
  }

  @GetMapping(value = BlogApiUrlConstant.LIST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getBlogList() {
    return blogService.getBlogList();
  }

  @GetMapping(value = BlogApiUrlConstant.DETAIL, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getBlogDetail(@RequestParam String code) {
    return blogService.getBlogDetail(code);
  }
}
