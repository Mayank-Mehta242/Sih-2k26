package Land.Slide.Early.Warn.system.LandSlidebyMayank.services;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.RoadSegment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.config.RiskScoringProperties;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RoadRiskResponse;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Error.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
    void reportsMissingInputsWithoutTreatingThemAsZeroRisk() {
        RoadSegment segment = validSegment();

        var result = service.assess(segment);

        assertNull(result.riskScore());
        assertEquals("UNKNOWN", result.riskLevel());
        assertEquals("INSUFFICIENT_DATA", result.assessmentStatus());
        assertEquals(5, result.dataQualityWarnings().size());
    }

    @Test
    void reportsOutdatedDataAsPartialAssessment() {
        RoadSegment segment = validSegment();
        segment.setRainfall(80.0);
        segment.setDataUpdatedAt(LocalDateTime.now().minusDays(2));

        var result = service.assess(segment);

        assertEquals("PARTIAL", result.assessmentStatus());
        assertTrue(result.dataQualityWarnings().stream().anyMatch(w -> w.contains("outdated")));
    }

    @Test
    void rejectsInvalidInputs() {
        RoadSegment segment = validSegment();
        segment.setRecentIncidents(-1);

        assertThrows(BadRequestException.class, () -> service.assess(segment));
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
