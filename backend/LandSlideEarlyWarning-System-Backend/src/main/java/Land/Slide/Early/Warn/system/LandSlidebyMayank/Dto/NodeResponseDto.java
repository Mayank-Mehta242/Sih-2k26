package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import lombok.Data;

import java.time.LocalDateTime;
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

    public NodeResponseDto() {
    }

    public NodeResponseDto(long id, String nodeName, double latitude, double longitude,
                           double soilMoisture, double tiltAngle, int rainDrops, double sound,
                           double vibrations, double temp, double humidity, boolean isActive,
                           LocalDateTime localDateTime) {
        this.id = id;
        this.nodeName = nodeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.soilMoisture = soilMoisture;
        this.tiltAngle = tiltAngle;
        this.rainDrops = rainDrops;
        this.sound = sound;
        this.vibrations = vibrations;
        this.temp = temp;
        this.humidity = humidity;
        this.isActive = isActive;
        this.localDateTime = localDateTime;
    }

}
