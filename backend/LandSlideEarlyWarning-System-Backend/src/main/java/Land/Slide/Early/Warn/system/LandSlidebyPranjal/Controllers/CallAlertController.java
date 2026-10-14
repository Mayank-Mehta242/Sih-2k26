package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Controllers;

import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.services.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/call")
public class CallAlertController {
   private final CallService callService;

    @PostMapping("/makeCall/{id}")
    public ResponseEntity<ResponseDto>makeCall(@PathVariable long id){
        return ResponseEntity.ok(callService.makeCall(id));
    }
}
