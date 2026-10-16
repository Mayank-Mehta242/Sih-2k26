package Land.Slide.Early.Warn.system.LandSlidebyMayank.Repository;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.NodeReadings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NodeHistoryRepository extends JpaRepository<NodeReadings,Long> {
    List<NodeReadings> findByNodeId(Long nodeId);
}
