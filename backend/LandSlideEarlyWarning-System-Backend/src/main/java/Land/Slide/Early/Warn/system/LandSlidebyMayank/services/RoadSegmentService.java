package Land.Slide.Early.Warn.system.LandSlidebyMayank.services;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RoadSegmentRequest;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.RoadSegment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Error.BadRequestException;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Error.RoadSegmentNotFoundException;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Repository.RoadSegmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoadSegmentService {
    private final RoadSegmentRepository repository;

    public RoadSegmentService(RoadSegmentRepository repository) {
        this.repository = repository;
    }

    public RoadSegment create(RoadSegmentRequest request) {
        RoadSegment segment = new RoadSegment();
        copy(request, segment);
        return repository.save(segment);
    }

    public List<RoadSegment> findAll() {
        return repository.findAll();
    }

    public RoadSegment findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RoadSegmentNotFoundException(id));
    }

    public RoadSegment update(Long id, RoadSegmentRequest request) {
        RoadSegment segment = findById(id);
        copy(request, segment);
        return repository.save(segment);
    }

    private void copy(RoadSegmentRequest request, RoadSegment segment) {
        segment.setRoadName(request.getRoadName());
        segment.setStartLatitude(request.getStartLatitude());
        segment.setStartLongitude(request.getStartLongitude());
        segment.setEndLatitude(request.getEndLatitude());
        segment.setEndLongitude(request.getEndLongitude());
        segment.setGeographicalLocation(request.getGeographicalLocation());
        segment.setRainfall(request.getRainfall());
        segment.setTerrainSusceptibility(request.getTerrainSusceptibility());
        segment.setRecentIncidents(request.getRecentIncidents());
        segment.setSensorRisk(request.getSensorRisk());
        segment.setDataUpdatedAt(request.getDataUpdatedAt());
    }
}
