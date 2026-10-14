package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Controllers;

import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.LandSlideReportDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity.LandSlideReports;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.services.LandSlideReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/landslide-report")
public class LandslideReportController {
    private final LandSlideReportService landSlideReportService;

    @PostMapping(value = "/makeReport" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> makeReport(@ModelAttribute LandSlideReportDto landSlideReportDto){
        return ResponseEntity.ok(landSlideReportService.createReport(landSlideReportDto));
    }

    @GetMapping("/getAllReports")
    public ResponseEntity<List<LandSlideReports>>getAllReports(){
        return ResponseEntity.ok(landSlideReportService.getAllReports());
    }
}
