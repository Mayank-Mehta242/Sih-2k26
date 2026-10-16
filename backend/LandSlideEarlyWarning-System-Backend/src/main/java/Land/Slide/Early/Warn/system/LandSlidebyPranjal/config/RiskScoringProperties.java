package Land.Slide.Early.Warn.system.LandSlidebyPranjal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "risk.scoring")
public class RiskScoringProperties {
    private double rainfallWeight = 30.0;
    private double terrainWeight = 25.0;
    private double incidentsWeight = 20.0;
    private double sensorWeight = 25.0;
    private int staleAfterHours = 24;
}
