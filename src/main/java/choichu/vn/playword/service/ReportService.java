package choichu.vn.playword.service;

import choichu.vn.playword.form.report.ReportForm;
import choichu.vn.playword.model.ReportEntity;
import choichu.vn.playword.repository.ReportRepository;
import java.util.Date;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

  private final ReportRepository reportRepository;

  public ReportService(ReportRepository reportRepository) {
    this.reportRepository = reportRepository;
  }

  public ResponseEntity<?> createReport(ReportForm form) {
    ReportEntity newReport = new ReportEntity();
    newReport.setWord(form.getWord());
    newReport.setIssueType(form.getIssueType());
    newReport.setCreatedDate(new Date());

    this.reportRepository.save(newReport);

    return ResponseEntity.ok(null);
  }
}
