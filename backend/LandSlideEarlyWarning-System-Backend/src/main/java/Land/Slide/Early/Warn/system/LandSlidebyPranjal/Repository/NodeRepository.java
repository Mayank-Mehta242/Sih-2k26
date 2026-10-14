package Land.Slide.Early.Warn.system.LandSlidebyPranjal.Repository;

import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node,Long> {

    Optional<Node> findByNodeName(String nodeName);
}
