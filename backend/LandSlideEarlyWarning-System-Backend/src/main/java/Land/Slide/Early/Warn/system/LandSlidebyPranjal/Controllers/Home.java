package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Controllers;

import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class Home {

        @GetMapping("/")
                public ResponseEntity<ResponseDto> home(){
            ResponseDto responseDto = new ResponseDto();
            return ResponseEntity.ok( new ResponseDto("server is running "));
    }
}
