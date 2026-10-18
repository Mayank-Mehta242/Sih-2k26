package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

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
        String message
) {
}
