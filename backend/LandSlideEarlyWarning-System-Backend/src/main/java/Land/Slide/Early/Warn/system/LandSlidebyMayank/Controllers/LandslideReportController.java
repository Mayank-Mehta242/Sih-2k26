package Land.Slide.Early.Warn.system.LandSlidebyMayank.Controllers;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.LandSlideReportDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.LandSlideReports;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.services.LandSlideReportService;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.services.SmsService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/landslide-report")
public class LandslideReportController {
    private final LandSlideReportService landSlideReportService;
    private final SmsService smsService;

    public LandslideReportController(LandSlideReportService landSlideReportService, SmsService smsService) {
        this.landSlideReportService = landSlideReportService;
        this.smsService = smsService;
    }

    @PostMapping(value = "/makeReport" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> makeReport(@ModelAttribute LandSlideReportDto landSlideReportDto){
        return ResponseEntity.ok(landSlideReportService.createReport(landSlideReportDto));
    }

    @GetMapping("/getAllReports")
    public ResponseEntity<List<LandSlideReports>>getAllReports(){
        return ResponseEntity.ok(landSlideReportService.getAllReports());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LandSlideReports> approveReport(
            @PathVariable long id,
            @RequestParam(defaultValue = "admin") String reviewedBy,
            @RequestParam(required = false) String reviewNote) {
        return ResponseEntity.ok(landSlideReportService.approveReport(id, reviewedBy, reviewNote));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LandSlideReports> rejectReport(
            @PathVariable long id,
            @RequestParam(defaultValue = "admin") String reviewedBy,
            @RequestParam(required = false) String reviewNote) {
        return ResponseEntity.ok(landSlideReportService.rejectReport(id, reviewedBy, reviewNote));
    }

    @PostMapping("/{id}/send-sms")
    public ResponseEntity<ResponseDto> sendSms(@PathVariable long id) {
        LandSlideReports report = landSlideReportService.getAllReports().stream()
                .filter(item -> item.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));
        String result = smsService.sendAdminSms(String.format(
                "Landslide incident #%d. Location: %.5f, %.5f. Please review the PahadSuraksha dashboard.",
                report.getId(), report.getLatitude(), report.getLongitude()));
        return ResponseEntity.ok(new ResponseDto(result));
    }
}
