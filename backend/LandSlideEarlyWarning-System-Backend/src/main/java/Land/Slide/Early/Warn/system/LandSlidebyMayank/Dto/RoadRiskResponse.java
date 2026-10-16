package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import java.util.List;

public record RoadRiskResponse(
        Long segmentId,
        Integer riskScore,
        String riskLevel,
        String assessmentStatus,
        List<RiskFactor> contributingFactors,
        List<String> dataQualityWarnings
) {
}
