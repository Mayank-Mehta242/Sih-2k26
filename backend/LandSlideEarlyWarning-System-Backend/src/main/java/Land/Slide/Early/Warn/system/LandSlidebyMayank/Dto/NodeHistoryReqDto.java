package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
public class NodeHistoryReqDto {
    private long nodeId;
    private double soilMoisture;
    private double tiltAngle;
    private int rainDrops;
    private double sound;
    private double vibrations;
    private double temp;
    private double humidity;

    public NodeHistoryReqDto(long nodeId, double soilMoisture, double tiltAngle,
                             int rainDrops, double sound, double vibrations,
                             double temp, double humidity) {
        this.nodeId = nodeId;
        this.soilMoisture = soilMoisture;
        this.tiltAngle = tiltAngle;
        this.rainDrops = rainDrops;
        this.sound = sound;
        this.vibrations = vibrations;
        this.temp = temp;
        this.humidity = humidity;
    }
}
