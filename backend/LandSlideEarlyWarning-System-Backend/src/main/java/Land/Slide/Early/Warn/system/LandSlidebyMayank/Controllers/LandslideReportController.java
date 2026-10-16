package Land.Slide.Early.Warn.system.LandSlidebyMayank.Controllers;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.LandSlideReportDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.LandSlideReports;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.services.LandSlideReportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/landslide-report")
public class LandslideReportController {
    private final LandSlideReportService landSlideReportService;

    public LandslideReportController(LandSlideReportService landSlideReportService) {
        this.landSlideReportService = landSlideReportService;
    }

    @PostMapping(value = "/makeReport" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> makeReport(@ModelAttribute LandSlideReportDto landSlideReportDto){
        return ResponseEntity.ok(landSlideReportService.createReport(landSlideReportDto));
    }

    @GetMapping("/getAllReports")
    public ResponseEntity<List<LandSlideReports>>getAllReports(){
        return ResponseEntity.ok(landSlideReportService.getAllReports());
    }
}
