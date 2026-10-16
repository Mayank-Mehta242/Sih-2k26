package Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
public class NodeReadings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double soilMoisture;
    private double tiltAngle;
    private int rainDrops;
    private double sound;
    private double vibrations;
    private double temp;
    private double humidity;

    private LocalDateTime localDateTime;

    @ManyToOne
    @JoinColumn(name = "node_id")
    private Node node;

    public NodeReadings() {
    }

    public NodeReadings(Long id, double soilMoisture, double tiltAngle, int rainDrops,
                        double sound, double vibrations, double temp, double humidity,
                        LocalDateTime localDateTime, Node node) {
        this.id = id;
        this.soilMoisture = soilMoisture;
        this.tiltAngle = tiltAngle;
        this.rainDrops = rainDrops;
        this.sound = sound;
        this.vibrations = vibrations;
        this.temp = temp;
        this.humidity = humidity;
        this.localDateTime = localDateTime;
        this.node = node;
    }
}
