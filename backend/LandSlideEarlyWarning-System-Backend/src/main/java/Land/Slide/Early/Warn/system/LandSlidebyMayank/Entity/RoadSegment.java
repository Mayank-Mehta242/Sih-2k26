package Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class RoadSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private Double rainfall;
    private Double terrainSusceptibility;
    private Integer recentIncidents;
    private Double sensorRisk;
    private LocalDateTime dataUpdatedAt;
}
