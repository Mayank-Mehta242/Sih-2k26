package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RouteRiskRequest(
        @NotEmpty(message = "segmentIds must contain at least one segment ID")
        List<@NotNull(message = "segment ID cannot be null") Long> segmentIds
) {
}
