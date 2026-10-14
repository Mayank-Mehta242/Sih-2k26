package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NodeRequestDto {
    private String nodeName;
    private double latitude;
    private double longitude;
}
