package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import java.util.List;
import java.time.LocalDateTime;

public record RoadRiskResponse(
        Long segmentId,
        Integer riskScore,
        String riskLevel,
        String assessmentStatus,
        List<RiskFactor> contributingFactors,
        List<String> dataQualityWarnings,
        LocalDateTime assessmentTimestamp,
        LocalDateTime dataUpdatedAt,
        String dataFreshness
) {
}
