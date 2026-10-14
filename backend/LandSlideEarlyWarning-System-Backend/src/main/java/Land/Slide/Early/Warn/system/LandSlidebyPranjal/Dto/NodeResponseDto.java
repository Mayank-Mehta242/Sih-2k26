package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NodeResponseDto {
    private long id;
    private String nodeName;
    private double latitude;
    private double longitude;
    private double soilMoisture;
    private double tiltAngle;
    private int rainDrops;
    private double sound;
    private double vibrations;
    private double temp;
    private double humidity;
    private boolean isActive;
    private LocalDateTime localDateTime;

}
