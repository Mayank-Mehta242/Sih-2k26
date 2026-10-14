package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Repository;

import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity.NodeReadings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NodeHistoryRepository extends JpaRepository<NodeReadings,Long> {
    List<NodeReadings> findByNodeId(Long nodeId);
}
