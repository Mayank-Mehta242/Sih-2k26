package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import java.time.LocalDateTime;
import java.util.List;

public record RouteComparisonResponse(
        String candidateId,
        Integer rank,
        boolean recommended,
        Double distanceKm,
        Double durationMinutes,
        Integer aggregateRiskScore,
        String aggregateRiskLevel,
        String assessmentStatus,
        int assessedSegmentCount,
        int missingSegmentCount,
        Double rankingScore,
        String message,
        List<RiskFactor> contributingFactors,
        List<String> dataQualityWarnings,
        LocalDateTime assessmentTimestamp
) {
}
