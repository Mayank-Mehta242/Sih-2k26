package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import lombok.Data;

@Data
public class NodeRequestDto {
    private String nodeName;
    private double latitude;
    private double longitude;

    public NodeRequestDto() {
    }

    public NodeRequestDto(String nodeName, double latitude, double longitude) {
        this.nodeName = nodeName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
