package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record RouteCandidateRequest(
        @NotBlank(message = "candidateId is required")
        String candidateId,
        @NotEmpty(message = "segmentIds must contain at least one segment ID")
        List<@NotNull(message = "segment ID cannot be null") Long> segmentIds,
        @NotNull(message = "distanceKm is required")
        @Positive(message = "distanceKm must be positive")
        Double distanceKm,
        @NotNull(message = "durationMinutes is required")
        @Positive(message = "durationMinutes must be positive")
        Double durationMinutes
) {
}
