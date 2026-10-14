package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
public class LandSlideReportDto {
    private String name;
    private String mobileNo;
    private String description;
    private Double latitude;
    private Double longitude;
    private MultipartFile image;

}
