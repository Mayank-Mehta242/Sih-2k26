package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.RoadSegment;

import java.util.List;

public record RouteCandidateAssessment(
        RouteCandidateRequest request,
        List<RoadSegment> segments
) {
}
