package choichu.vn.playword.repository;

import choichu.vn.playword.dto.blog.LiteBlogDTO;
import choichu.vn.playword.model.BlogEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<BlogEntity, Long> {

  BlogEntity findByCode(String code);

  @Query(value =
      "SELECT new choichu.vn.playword.dto.blog.LiteBlogDTO("
      + " b.code, b.title, b.description, b.image, b.createdDate, b.readNum) "
      + "FROM BlogEntity b "
      + "ORDER BY b.createdDate DESC")
  List<LiteBlogDTO> getLiteBlogList(Pageable pageable);
}