package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import java.time.LocalDateTime;

public class NodeHistoryResDto {
    private long nodeId;
    private double soilMoisture;
    private double tiltAngle;
    private int rainDrops;
    private double sound;
    private double vibrations;
    private double temp;
    private double humidity;
    private LocalDateTime localDateTime;

    public NodeHistoryResDto() {
    }

    public NodeHistoryResDto(long nodeId, double soilMoisture, double tiltAngle, int rainDrops,
                             double sound, double vibrations, double temp, double humidity,
                             LocalDateTime localDateTime) {
        this.nodeId = nodeId;
        this.soilMoisture = soilMoisture;
        this.tiltAngle = tiltAngle;
        this.rainDrops = rainDrops;
        this.sound = sound;
        this.vibrations = vibrations;
        this.temp = temp;
        this.humidity = humidity;
        this.localDateTime = localDateTime;
    }

    public long getNodeId() { return nodeId; }
    public void setNodeId(long nodeId) { this.nodeId = nodeId; }
    public double getSoilMoisture() { return soilMoisture; }
    public void setSoilMoisture(double soilMoisture) { this.soilMoisture = soilMoisture; }
    public double getTiltAngle() { return tiltAngle; }
    public void setTiltAngle(double tiltAngle) { this.tiltAngle = tiltAngle; }
    public int getRainDrops() { return rainDrops; }
    public void setRainDrops(int rainDrops) { this.rainDrops = rainDrops; }
    public double getSound() { return sound; }
    public void setSound(double sound) { this.sound = sound; }
    public double getVibrations() { return vibrations; }
    public void setVibrations(double vibrations) { this.vibrations = vibrations; }
    public double getTemp() { return temp; }
    public void setTemp(double temp) { this.temp = temp; }
    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    public LocalDateTime getLocalDateTime() { return localDateTime; }
    public void setLocalDateTime(LocalDateTime localDateTime) { this.localDateTime = localDateTime; }
}
