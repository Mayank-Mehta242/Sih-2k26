package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto;

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
