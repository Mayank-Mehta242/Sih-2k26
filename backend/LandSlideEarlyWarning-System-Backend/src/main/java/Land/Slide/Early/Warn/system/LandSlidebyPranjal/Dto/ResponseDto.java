package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
public class ResponseDto {
    private String message;

    public ResponseDto(String s) {
        this.setMessage(s);
    }
}