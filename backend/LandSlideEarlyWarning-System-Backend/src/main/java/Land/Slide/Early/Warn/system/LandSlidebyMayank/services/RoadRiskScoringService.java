package Land.Slide.Early.Warn.system.LandSlidebyMayank.services;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RiskFactor;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RoadRiskResponse;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RouteCandidateAssessment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RouteCandidateRequest;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RouteComparisonResponse;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.RoadSegment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Error.BadRequestException;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.config.RiskScoringProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
            Double value = validMeasurement("rainfall", segment.getRainfall(), 0, 100, warnings);
            if (value != null) {
                weightedScore += value * properties.getRainfallWeight();
                availableWeight += properties.getRainfallWeight();
                factors.add(factor("Rainfall", value, properties.getRainfallWeight()));
            }
        } else {
            warnings.add("Rainfall data is missing; this factor cannot support a safe assessment.");
        }

        if (segment.getTerrainSusceptibility() != null) {
            Double value = validMeasurement("terrain susceptibility", segment.getTerrainSusceptibility(), 0, 100, warnings);
            if (value != null) {
                weightedScore += value * properties.getTerrainWeight();
                availableWeight += properties.getTerrainWeight();
                factors.add(factor("Terrain susceptibility", value, properties.getTerrainWeight()));
            }
        } else {
            warnings.add("Terrain susceptibility data is missing; this factor cannot support a safe assessment.");
        }

        if (segment.getRecentIncidents() != null) {
            Double value = validMeasurement("recent incidents", segment.getRecentIncidents(), 0, Double.MAX_VALUE, warnings);
            if (value != null) {
                value = Math.min(value * 25.0, 100.0);
                weightedScore += value * properties.getIncidentsWeight();
                availableWeight += properties.getIncidentsWeight();
                factors.add(factor("Recent incidents", value, properties.getIncidentsWeight()));
            }
        } else {
            warnings.add("Recent incident data is missing; this factor cannot support a safe assessment.");
        }

        if (segment.getSensorRisk() != null) {
            Double value = validMeasurement("sensor risk", segment.getSensorRisk(), 0, 100, warnings);
            if (value != null) {
                weightedScore += value * properties.getSensorWeight();
                availableWeight += properties.getSensorWeight();
                factors.add(factor("Sensor risk", value, properties.getSensorWeight()));
            }
        } else {
            warnings.add("Sensor readings are missing; this factor cannot support a safe assessment.");
        }

        if (segment.getDataUpdatedAt() == null) {
            warnings.add("Data timestamp is missing; freshness cannot be verified.");
        } else if (segment.getDataUpdatedAt().isAfter(assessmentTimestamp)) {
            warnings.add("Data timestamp is in the future and cannot be trusted.");
        } else if (segment.getDataUpdatedAt().isBefore(
                assessmentTimestamp.minusHours(properties.getStaleAfterHours()))) {
            warnings.add("Risk data is outdated and cannot support a reliable route recommendation.");
        }

        if (availableWeight == 0 || !warnings.isEmpty()) {
            String explanation = availableWeight == 0
                    ? "Risk score cannot be calculated reliably because no valid, fresh risk inputs are available."
                    : "Risk score is withheld because one or more risk inputs are missing, invalid, or not fresh enough.";
            return new RoadRiskResponse(segment.getId(), null, "UNKNOWN",
                    "INSUFFICIENT_DATA", factors, warnings, assessmentTimestamp,
                    segment.getDataUpdatedAt(), freshness(segment, warnings), explanation, List.of());
        }

        int score = (int) Math.round(Math.max(0, Math.min(100,
                weightedScore / availableWeight)));
        List<String> riskWarnings = riskWarnings(score);
        return new RoadRiskResponse(segment.getId(), score, riskLevel(score),
                "COMPLETE", factors, warnings, assessmentTimestamp,
                segment.getDataUpdatedAt(), freshness(segment, warnings),
                "Reliable risk inputs produce a " + riskLevel(score).toLowerCase()
                        + " risk score; this is an assessment, not a road-closure confirmation.",
                riskWarnings);
    }

    public List<RoadRiskResponse> assessRoute(List<RoadSegment> segments) {
        return segments.stream().map(this::assess).toList();
    }

    /**
     * Ranks routes conservatively: the highest assessed segment score is the
     * route risk, and any route with an unknown segment is ineligible for the
     * lowest-risk recommendation. Distance and duration are normalized against
     * the submitted candidates and combined with configurable weights.
     */
    public List<RouteComparisonResponse> compareRoutes(List<RouteCandidateAssessment> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            throw new BadRequestException("At least one route candidate is required.");
        }
        long distinctIds = candidates.stream()
                .map(candidate -> candidate.request().candidateId())
                .distinct()
                .count();
        if (distinctIds != candidates.size()) {
            throw new BadRequestException("candidateId values must be unique.");
        }
        double maxDistance = candidates.stream()
                .mapToDouble(candidate -> candidate.request().distanceKm())
                .max().orElse(1);
        double maxDuration = candidates.stream()
                .mapToDouble(candidate -> candidate.request().durationMinutes())
                .max().orElse(1);

        List<RankedRoute> ranked = candidates.stream()
                .map(candidate -> rank(candidate, maxDistance, maxDuration))
                .sorted(Comparator.comparing((RankedRoute route) -> route.rankingScore(),
                        (left, right) -> {
                            if (left == null) {
                                return right == null ? 0 : 1;
                            }
                            if (right == null) {
                                return -1;
                            }
                            return Double.compare(left, right);
                        }))
                .toList();
        Map<String, Integer> ranks = ranked.stream().collect(Collectors.toMap(
                route -> route.candidate().request().candidateId(),
                route -> ranked.indexOf(route) + 1
        ));
        String recommendedId = ranked.stream()
                .filter(route -> route.rankingScore() != null)
                .map(route -> route.candidate().request().candidateId())
                .findFirst().orElse(null);

        return ranked.stream().map(route -> {
            RouteCandidateRequest request = route.candidate().request();
            boolean complete = route.missingSegmentCount() == 0;
            boolean recommended = route.rankingScore() != null
                    && route.rankingScore().equals(ranked.get(0).rankingScore())
                    && request.candidateId().equals(recommendedId);
            return new RouteComparisonResponse(
                    request.candidateId(),
                    ranks.get(request.candidateId()),
                    recommended,
                    request.distanceKm(),
                    request.durationMinutes(),
                    route.aggregateRiskScore(),
                    route.aggregateRiskLevel(),
                    complete ? "COMPLETE" : "INCOMPLETE",
                    route.assessedSegmentCount(),
                    route.missingSegmentCount(),
                    route.rankingScore(),
                    complete
                            ? "Risk is an assessment, not a guarantee of safety."
                            : "Not recommended: one or more segments have unavailable or unreliable risk data.",
                    route.contributingFactors(),
                    route.dataQualityWarnings(),
                    route.assessmentTimestamp()
            );
        }).toList();
    }

    private RankedRoute rank(RouteCandidateAssessment candidate, double maxDistance, double maxDuration) {
        List<RoadRiskResponse> assessments = assessRoute(candidate.segments());
        int missing = (int) assessments.stream().filter(response -> response.riskScore() == null).count();
        if (missing > 0) {
            return new RankedRoute(candidate, null, null, null,
                    assessments.size() - missing, missing, factors(assessments),
                    warnings(assessments), latestAssessment(assessments));
        }
        int aggregate = assessments.stream()
            .mapToInt(response -> response.riskScore() == null ? 100 : response.riskScore())
            .max()
            .orElse(100);
        double distanceScore = candidate.request().distanceKm() / maxDistance * 100;
        double durationScore = candidate.request().durationMinutes() / maxDuration * 100;
        double rankingScore = aggregate * properties.getRouteRiskWeight()
                + distanceScore * properties.getRouteDistanceWeight()
                + durationScore * properties.getRouteDurationWeight();
        return new RankedRoute(candidate, aggregate, riskLevel(aggregate), rankingScore,
                assessments.size(), 0, factors(assessments), warnings(assessments),
                latestAssessment(assessments));
    }

    private record RankedRoute(
            RouteCandidateAssessment candidate,
            Integer aggregateRiskScore,
            String aggregateRiskLevel,
            Double rankingScore,
            int assessedSegmentCount,
            int missingSegmentCount,
            List<RiskFactor> contributingFactors,
            List<String> dataQualityWarnings,
            LocalDateTime assessmentTimestamp
    ) {
    }

    private String freshness(RoadSegment segment, List<String> warnings) {
        if (segment.getDataUpdatedAt() == null) {
            return "UNKNOWN";
        }
        if (warnings.stream().anyMatch(warning -> warning.contains("future"))) {
            return "INVALID";
        }
        return warnings.stream().anyMatch(warning -> warning.contains("outdated"))
                ? "STALE"
                : "FRESH";
    }

    private RiskFactor factor(String name, double normalizedValue, double weight) {
        return new RiskFactor(name, Math.round(normalizedValue * weight) / 100.0,
                name + " contributes " + Math.round(normalizedValue * weight) / 100.0
                        + " points from an input risk of " + Math.round(normalizedValue) + "/100.");
    }

    private Double validMeasurement(String name, double value, double min, double max,
                                    List<String> warnings) {
        if (!Double.isFinite(value) || value < min || value > max) {
            warnings.add(name + " data is invalid and was excluded from scoring.");
            return null;
        }
        return value;
    }

    private List<String> riskWarnings(int score) {
        if (score >= 75) {
            return List.of("CRITICAL risk warning: avoid relying on this segment without official guidance.");
        }
        if (score >= 50) {
            return List.of("HIGH risk warning: use caution and check current official road guidance.");
        }
        return List.of();
    }

    private List<RiskFactor> factors(List<RoadRiskResponse> assessments) {
        return assessments.stream()
                .flatMap(assessment -> assessment.contributingFactors().stream())
                .sorted(Comparator.comparingDouble((RiskFactor factor) -> factor.contribution()).reversed())
                .toList();
    }

    private List<String> warnings(List<RoadRiskResponse> assessments) {
        return assessments.stream()
                .flatMap(assessment -> assessment.dataQualityWarnings().stream())
                .distinct()
                .toList();
    }

    private LocalDateTime latestAssessment(List<RoadRiskResponse> assessments) {
        return assessments.stream()
                .map(assessment -> assessment.assessmentTimestamp())
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
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
