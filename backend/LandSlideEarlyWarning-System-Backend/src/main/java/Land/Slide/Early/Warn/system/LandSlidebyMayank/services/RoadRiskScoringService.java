package Land.Slide.Early.Warn.system.LandSlidebyMayank.services;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RiskFactor;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RoadRiskResponse;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.RoadSegment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Error.BadRequestException;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.config.RiskScoringProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoadRiskScoringService {
    private final RiskScoringProperties properties;

    public RoadRiskScoringService(RiskScoringProperties properties) {
        this.properties = properties;
    }

    public RoadRiskResponse assess(RoadSegment segment) {
        validateSegment(segment);
        LocalDateTime assessmentTimestamp = LocalDateTime.now();

        List<RiskFactor> factors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        double weightedScore = 0;
        double availableWeight = 0;

        if (segment.getRainfall() != null) {
            double value = validateRange("rainfall", segment.getRainfall(), 0, 100);
            weightedScore += value * properties.getRainfallWeight();
            availableWeight += properties.getRainfallWeight();
            factors.add(factor("rainfall", value, properties.getRainfallWeight()));
        } else {
            warnings.add("Rainfall data is missing.");
        }

        if (segment.getTerrainSusceptibility() != null) {
            double value = validateRange("terrainSusceptibility", segment.getTerrainSusceptibility(), 0, 100);
            weightedScore += value * properties.getTerrainWeight();
            availableWeight += properties.getTerrainWeight();
            factors.add(factor("terrainSusceptibility", value, properties.getTerrainWeight()));
        } else {
            warnings.add("Terrain susceptibility data is missing.");
        }

        if (segment.getRecentIncidents() != null) {
            if (segment.getRecentIncidents() < 0) {
                throw new BadRequestException("recentIncidents cannot be negative.");
            }
            double value = Math.min(segment.getRecentIncidents() * 25.0, 100.0);
            weightedScore += value * properties.getIncidentsWeight();
            availableWeight += properties.getIncidentsWeight();
            factors.add(factor("recentIncidents", value, properties.getIncidentsWeight()));
        } else {
            warnings.add("Recent incident data is missing.");
        }

        if (segment.getSensorRisk() != null) {
            double value = validateRange("sensorRisk", segment.getSensorRisk(), 0, 100);
            weightedScore += value * properties.getSensorWeight();
            availableWeight += properties.getSensorWeight();
            factors.add(factor("sensorRisk", value, properties.getSensorWeight()));
        } else {
            warnings.add("Sensor readings are missing.");
        }

        if (segment.getDataUpdatedAt() == null) {
            warnings.add("Data timestamp is missing; freshness cannot be verified.");
        } else if (segment.getDataUpdatedAt().isBefore(
                LocalDateTime.now().minusHours(properties.getStaleAfterHours()))) {
            warnings.add("Risk data is outdated.");
        }

        if (availableWeight == 0) {
            return new RoadRiskResponse(segment.getId(), null, "UNKNOWN",
                    "INSUFFICIENT_DATA", factors, warnings, assessmentTimestamp,
                    segment.getDataUpdatedAt(), freshness(segment, warnings));
        }

        int score = (int) Math.round(Math.max(0, Math.min(100,
                weightedScore / availableWeight)));
        String status = warnings.isEmpty() ? "COMPLETE" : "PARTIAL";
        return new RoadRiskResponse(segment.getId(), score, riskLevel(score),
                status, factors, warnings, assessmentTimestamp,
                segment.getDataUpdatedAt(), freshness(segment, warnings));
    }

    public List<RoadRiskResponse> assessRoute(List<RoadSegment> segments) {
        return segments.stream().map(this::assess).toList();
    }

    private String freshness(RoadSegment segment, List<String> warnings) {
        if (segment.getDataUpdatedAt() == null) {
            return "UNKNOWN";
        }
        return warnings.stream().anyMatch(warning -> warning.contains("outdated"))
                ? "STALE"
                : "FRESH";
    }

    private RiskFactor factor(String name, double normalizedValue, double weight) {
        return new RiskFactor(name, Math.round(normalizedValue * weight) / 100.0,
                "Input risk " + Math.round(normalizedValue) + "/100; weight " + weight + "%.");
    }

    private String riskLevel(int score) {
        if (score <= 24) return "LOW";
        if (score <= 49) return "MODERATE";
        if (score <= 74) return "HIGH";
        return "CRITICAL";
    }

    private double validateRange(String name, double value, double min, double max) {
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new BadRequestException(name + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    private void validateSegment(RoadSegment segment) {
        if (segment == null || segment.getRoadName() == null || segment.getRoadName().isBlank()) {
            throw new BadRequestException("roadName is required.");
        }
        validateRange("startLatitude", segment.getStartLatitude(), -90, 90);
        validateRange("startLongitude", segment.getStartLongitude(), -180, 180);
        validateRange("endLatitude", segment.getEndLatitude(), -90, 90);
        validateRange("endLongitude", segment.getEndLongitude(), -180, 180);
    }
}
