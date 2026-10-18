package Land.Slide.Early.Warn.system.LandSlidebyMayank.Controllers;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RoadRiskResponse;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RoadSegmentRequest;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RouteRiskRequest;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RouteCandidateAssessment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RouteComparisonRequest;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RouteComparisonResponse;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.RoadSegment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.services.RoadRiskScoringService;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.services.RoadSegmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/road-segments")
public class RoadSegmentController {
    private final RoadSegmentService segmentService;
    private final RoadRiskScoringService riskService;

    public RoadSegmentController(RoadSegmentService segmentService,
                                 RoadRiskScoringService riskService) {
        this.segmentService = segmentService;
        this.riskService = riskService;
    }

    @PostMapping
    public ResponseEntity<RoadSegment> create(@Valid @RequestBody RoadSegmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(segmentService.create(request));
    }

    @GetMapping
    public List<RoadSegment> findAll() {
        return segmentService.findAll();
    }

    @PutMapping("/{id}")
    public RoadSegment update(@PathVariable Long id, @Valid @RequestBody RoadSegmentRequest request) {
        return segmentService.update(id, request);
    }

    @GetMapping("/{id}/risk")
    public RoadRiskResponse risk(@PathVariable Long id) {
        return riskService.assess(segmentService.findById(id));
    }

    @PostMapping("/risk/assess")
    public List<RoadRiskResponse> assessRoute(@Valid @RequestBody RouteRiskRequest request) {
        return riskService.assessRoute(request.segmentIds().stream()
                .map(segmentService::findById)
                .toList());
    }

    @PostMapping("/risk/compare")
    public List<RouteComparisonResponse> compareRoutes(
            @Valid @RequestBody RouteComparisonRequest request) {
        return riskService.compareRoutes(request.candidates().stream()
                .map(candidate -> new RouteCandidateAssessment(
                        candidate,
                        candidate.segmentIds().stream().map(segmentService::findById).toList()))
                .toList());
    }
}
