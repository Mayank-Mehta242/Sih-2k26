package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RouteComparisonRequest(
        @NotEmpty(message = "candidates must contain at least one route")
        List<@Valid RouteCandidateRequest> candidates
) {
}
