package Land.Slide.Early.Warn.system.LandSlidebyMayank.services;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.RoadSegment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.config.RiskScoringProperties;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RoadRiskResponse;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RouteCandidateAssessment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RouteCandidateRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoadRiskScoringServiceTest {
    private final RoadRiskScoringService service = new RoadRiskScoringService(new RiskScoringProperties());

    @Test
    void calculatesEachConfiguredRiskLevel() {
        assertEquals("LOW", assessWithValue(10).riskLevel());
        assertEquals("MODERATE", assessWithValue(35).riskLevel());
        assertEquals("HIGH", assessWithValue(60).riskLevel());
        assertEquals("CRITICAL", assessWithValue(90).riskLevel());
    }

    @Test
    void keepsRiskCategoryBoundariesInclusive() {
        assertEquals("LOW", assessWithValue(24).riskLevel());
        assertEquals("MODERATE", assessWithValue(49).riskLevel());
        assertEquals("HIGH", assessWithValue(74).riskLevel());
        assertEquals("CRITICAL", assessWithValue(75).riskLevel());
    }

    @Test
    void reportsMissingInputsWithoutTreatingThemAsZeroRisk() {
        RoadSegment segment = validSegment();

        var result = service.assess(segment);

        assertNull(result.riskScore());
        assertEquals("UNKNOWN", result.riskLevel());
        assertEquals("INSUFFICIENT_DATA", result.assessmentStatus());
        assertEquals(5, result.dataQualityWarnings().size());
    }

    @Test
    void withholdsScoreForOutdatedData() {
        RoadSegment segment = validSegment();
        segment.setRainfall(80.0);
        segment.setDataUpdatedAt(LocalDateTime.now().minusDays(2));

        var result = service.assess(segment);

        assertNull(result.riskScore());
        assertEquals("INSUFFICIENT_DATA", result.assessmentStatus());
        assertTrue(result.dataQualityWarnings().stream().anyMatch(w -> w.contains("outdated")));
        assertTrue(result.explanation().contains("withheld"));
    }

    @Test
    void flagsInvalidRiskInputsWithoutTreatingThemAsSafe() {
        RoadSegment segment = validSegment();
        segment.setRecentIncidents(-1);

        var result = service.assess(segment);

        assertNull(result.riskScore());
        assertTrue(result.dataQualityWarnings().stream().anyMatch(w -> w.contains("invalid")));
    }

    @Test
    void flagsInvalidRainfallAndSensorInputs() {
        RoadSegment segment = assessedSegment(20);
        segment.setRainfall(Double.NaN);
        segment.setSensorRisk(101.0);

        var result = service.assess(segment);

        assertNull(result.riskScore());
        assertEquals(2, result.dataQualityWarnings().stream()
                .filter(w -> w.contains("invalid")).count());
    }

    @Test
    void ranksRoutesByHighestSegmentRiskAndKeepsUnknownRoutesOutOfRecommendation() {
        RoadSegment low = assessedSegment(10.0);
        low.setId(1L);
        RoadSegment unknown = validSegment();
        unknown.setId(2L);

        var result = service.compareRoutes(List.of(
                new RouteCandidateAssessment(
                        new RouteCandidateRequest("fast-unknown", List.of(2L), 5.0, 8.0),
                        List.of(unknown)),
                new RouteCandidateAssessment(
                        new RouteCandidateRequest("slower-low-risk", List.of(1L), 8.0, 14.0),
                        List.of(low))
        ));

        assertEquals("slower-low-risk", result.get(0).candidateId());
        assertTrue(result.get(0).recommended());
        assertEquals(8, result.get(0).aggregateRiskScore());
        assertEquals("INCOMPLETE", result.get(1).assessmentStatus());
        assertFalse(result.get(1).recommended());
        assertNull(result.get(1).rankingScore());
    }

    private RoadSegment assessedSegment(double value) {
        RoadSegment segment = validSegment();
        segment.setRainfall(value);
        segment.setTerrainSusceptibility(value);
        segment.setRecentIncidents(0);
        segment.setSensorRisk(value);
        segment.setDataUpdatedAt(LocalDateTime.now());
        return segment;
    }

    private RoadRiskResponse assessWithValue(double value) {
        RoadSegment segment = validSegment();
        segment.setRainfall(value);
        segment.setTerrainSusceptibility(value);
        segment.setRecentIncidents((int) Math.round(value / 25.0));
        segment.setSensorRisk(value);
        segment.setDataUpdatedAt(LocalDateTime.now());
        return service.assess(segment);
    }

    private RoadSegment validSegment() {
        RoadSegment segment = new RoadSegment();
        segment.setRoadName("NH-07");
        segment.setStartLatitude(30.1);
        segment.setStartLongitude(78.1);
        segment.setEndLatitude(30.2);
        segment.setEndLongitude(78.2);
        return segment;
    }
}
