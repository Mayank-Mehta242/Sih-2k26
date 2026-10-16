package Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
public class Node {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
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

   public Node(){

   }

}
