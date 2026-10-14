package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class LandSlideReports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String mobileNo;
    @Column(columnDefinition = "TEXT")
    private String Description;
    private double latitude;
    private double longitude;
    private String imageUrl;


}
