package Land.Slide.Early.Warn.system.LandSlidebyPranjal.services;

import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.NodeIncomingDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.NodeRequestDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.NodeResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity.Node;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Error.NodeNotFoundException;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Repository.NodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NodeService {
     private final NodeRepository nodeRepository;
     public NodeService(NodeRepository nodeRepository){
         this.nodeRepository=nodeRepository;
     }

     // add node;
    public ResponseDto createNode(NodeRequestDto nodeRequestDto){
        Node node = new Node();
        node.setNodeName(nodeRequestDto.getNodeName());
        node.setLatitude(nodeRequestDto.getLatitude());
        node.setLongitude(nodeRequestDto.getLongitude());
        nodeRepository.save(node);
        return  new ResponseDto("node "+ node.getNodeName()+" created");
    }


    //delete node
    public ResponseDto deleteNode(long id){
        Node node = nodeRepository.findById(id).orElseThrow(()-> new NodeNotFoundException(
                "Node with Id "+ id+"not found "
        ));

        nodeRepository.delete(node);
        return new ResponseDto("node deleted successfully");

    }
    //set live data

    public ResponseDto setNodeData(NodeIncomingDto nodeIncomingDto){
         Node node = nodeRepository.findById(nodeIncomingDto.getId()).orElseThrow(()-> new NodeNotFoundException(
                 "Node with Id "+nodeIncomingDto.getId()+"not found "
         ));


             node.setSoilMoisture(nodeIncomingDto.getSoilMoisture());
             node.setTiltAngle(nodeIncomingDto.getTiltAngle());
             node.setRainDrops(nodeIncomingDto.getRainDrops());
             node.setSound(nodeIncomingDto.getSound());
             node.setVibrations(nodeIncomingDto.getVibrations());
             node.setTemp(nodeIncomingDto.getTemp());
             node.setHumidity(nodeIncomingDto.getHumidity());
             node.setActive(true);
             node.setLocalDateTime(LocalDateTime.now());

             nodeRepository.save(node);

             return new ResponseDto("node data is updated");
    }

    // get live data

    public Node getLiveData(String nodeName){
         Node node = nodeRepository.findByNodeName(nodeName) .orElseThrow(() -> new NodeNotFoundException(
                 "Node with name " + nodeName + " not found"
         ));
         return node;
    }

    // get ALl Nodes Data

    public List<Node>getAllNodes(){
         return nodeRepository.findAll();
    }

}
