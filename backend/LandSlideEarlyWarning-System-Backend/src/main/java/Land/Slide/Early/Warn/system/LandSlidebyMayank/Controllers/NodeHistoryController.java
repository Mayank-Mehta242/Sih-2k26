package Land.Slide.Early.Warn.system.LandSlidebyMayank.Controllers;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.NodeHistoryReqDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.NodeHistoryResDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.services.NodeHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nodeHistory")
public class NodeHistoryController {
    private final NodeHistoryService nodeHistoryService;
    public NodeHistoryController(NodeHistoryService nodeHistoryService){
        this.nodeHistoryService=nodeHistoryService;
    }
    @PostMapping("/post")
    public ResponseEntity<ResponseDto> postHistory(@RequestBody NodeHistoryReqDto nodeHistoryReqDto){
        return  ResponseEntity.ok(nodeHistoryService.addNodeHistory(nodeHistoryReqDto));
    }

    @GetMapping("/getHistory/{nodeId}")
    public ResponseEntity<List<NodeHistoryResDto>>getNodeHistory(@PathVariable long nodeId){
        return ResponseEntity.ok(nodeHistoryService.getNodeHistory(nodeId));
    }
}
