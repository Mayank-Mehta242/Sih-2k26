package Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    public LandSlideReports() {
    }

    public LandSlideReports(long id, String name, String mobileNo, String description,
                            double latitude, double longitude, String imageUrl) {
        this.id = id;
        this.name = name;
        this.mobileNo = mobileNo;
        this.Description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
    }
}
