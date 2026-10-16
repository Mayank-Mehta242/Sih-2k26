package Land.Slide.Early.Warn.system.LandSlidebyMayank.Controllers;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.services.CallService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/call")
public class CallAlertController {
    private final CallService callService;

    public CallAlertController(CallService callService) {
        this.callService = callService;
    }

    @PostMapping("/makeCall/{id}")
    public ResponseEntity<ResponseDto> makeCall(@PathVariable long id) {
        return ResponseEntity.ok(callService.makeCall(id));
    }
}
