package Land.Slide.Early.Warn.system.LandSlidebyMayank.Controllers;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.NodeIncomingDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.NodeRequestDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.Node;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.services.NodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nodes")
public class NodeController {
    private final NodeService nodeService;

    public NodeController(NodeService nodeService){
        this.nodeService=nodeService;
    }

    @PostMapping("/addNode")
    public ResponseEntity<ResponseDto>createNode(@RequestBody NodeRequestDto nodeRequestDto){
       return ResponseEntity.ok(nodeService.createNode(nodeRequestDto));
    }

    @PutMapping("/updateNodeLatest")
    public ResponseEntity<ResponseDto>updateNode(@RequestBody NodeIncomingDto nodeIncomingDto){
        return  ResponseEntity.ok(nodeService.setNodeData(nodeIncomingDto));
    }
    @DeleteMapping("/deleteNode/{id}")
    public ResponseEntity<ResponseDto>deleteNode(@PathVariable long id){
        return ResponseEntity.ok(nodeService.deleteNode(id));
    }

    @GetMapping("/getbyName/{nodeName}")

    public ResponseEntity<Node>getDataById(@PathVariable String nodeName ){
        return  ResponseEntity.ok(nodeService.getLiveData(nodeName));
    }

    @GetMapping("/getAllNodes")
    public ResponseEntity<List<Node>>getAllNodes(){
        return ResponseEntity.ok(nodeService.getAllNodes());
    }

}
