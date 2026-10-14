package Land.Slide.Early.Warn.system.LandSlidebyPranjal.services;

import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.NodeHistoryReqDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.NodeHistoryResDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity.Node;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity.NodeReadings;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Error.NodeNotFoundException;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Repository.NodeHistoryRepository;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Repository.NodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class NodeHistoryService {
   private final NodeHistoryRepository nodeHistoryRepository;
   private final NodeRepository nodeRepository;
   public NodeHistoryService(NodeHistoryRepository nodeHistoryRepository, NodeRepository nodeRepository){
       this.nodeHistoryRepository=nodeHistoryRepository;
       this.nodeRepository=nodeRepository;
   }


//post Node History

    public ResponseDto addNodeHistory(NodeHistoryReqDto nodeHistoryReqDto){
       Node node = nodeRepository.findById(nodeHistoryReqDto.getNodeId()).orElseThrow(
               ()-> new NodeNotFoundException(   "Node with Id " + nodeHistoryReqDto.getNodeId() + " not found")
       );
       NodeReadings nodeReadings = new NodeReadings();
        nodeReadings.setNode(node);
        nodeReadings.setSoilMoisture(nodeHistoryReqDto.getSoilMoisture());
        nodeReadings.setTiltAngle(nodeHistoryReqDto.getTiltAngle());
        nodeReadings.setRainDrops(nodeHistoryReqDto.getRainDrops());
        nodeReadings.setSound(nodeHistoryReqDto.getSound());
        nodeReadings.setVibrations(nodeHistoryReqDto.getVibrations());
        nodeReadings.setTemp(nodeHistoryReqDto.getTemp());
        nodeReadings.setHumidity(nodeHistoryReqDto.getHumidity());

        nodeReadings.setLocalDateTime(LocalDateTime.now());

        nodeHistoryRepository.save(nodeReadings);

        return new ResponseDto("Node history added successfully");
    }

   //get Node History

    public List<NodeHistoryResDto> getNodeHistory(long nodeId){
        Node node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new NodeNotFoundException(
                        "Node with Id " + nodeId + " not found"
                ));

        List<NodeReadings> nodeReadingsList = nodeHistoryRepository.findByNodeId(nodeId);
       return  nodeReadingsList.stream().map(reading-> new NodeHistoryResDto(
               node.getId(),
               reading.getSoilMoisture(),
               reading.getTiltAngle(),
               reading.getRainDrops(),
               reading.getSound(),
               reading.getVibrations(),
               reading.getTemp(),
               reading.getHumidity(),
               reading.getLocalDateTime()
       )) .toList();
    }
    }


