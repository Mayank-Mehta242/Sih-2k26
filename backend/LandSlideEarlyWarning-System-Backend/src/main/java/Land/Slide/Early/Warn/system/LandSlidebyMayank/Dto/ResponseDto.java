package Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseDto {
    private String message;

    public ResponseDto(String s) {
        this.setMessage(s);
    }
}