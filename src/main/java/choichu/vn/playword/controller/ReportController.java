package choichu.vn.playword.controller;

import choichu.vn.playword.constant.CommonConstant;
import choichu.vn.playword.constant.ReportApiUrlConstant;
import choichu.vn.playword.form.report.ReportForm;
import choichu.vn.playword.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = "*")
@RequestMapping(value = CommonConstant.BASE_API_URL)
@RestController
@Slf4j
public class ReportController {

  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  @PostMapping(value = ReportApiUrlConstant.CREATE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> createReport(@RequestBody ReportForm form) {
    return reportService.createReport(form);
  }
}
