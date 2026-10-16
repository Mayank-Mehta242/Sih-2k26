package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoadSegmentRequest {
    @NotBlank
    private String roadName;
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private double startLatitude;
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private double startLongitude;
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private double endLatitude;
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private double endLongitude;
    private String geographicalLocation;
    @PositiveOrZero
    private Double rainfall;
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double terrainSusceptibility;
    @PositiveOrZero
    private Integer recentIncidents;
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double sensorRisk;
    private LocalDateTime dataUpdatedAt;
}
