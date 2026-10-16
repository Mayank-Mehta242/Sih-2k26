package Land.Slide.Early.Warn.system.LandSlidebyMayank.config;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.Node;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.NodeReadings;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.RoadSegment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Repository.NodeHistoryRepository;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Repository.NodeRepository;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Repository.RoadSegmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Configuration
@Profile("local")
public class LocalDemoDataConfig {

    @Bean
    CommandLineRunner seedLocalDemoData(
            NodeRepository nodeRepository,
            NodeHistoryRepository nodeHistoryRepository,
            RoadSegmentRepository roadSegmentRepository
    ) {
        return args -> {
            if (nodeRepository.count() == 0) {
                Node node = new Node();
                node.setNodeName("Tehri Ridge Alpha");
                node.setLatitude(30.3782);
                node.setLongitude(78.4804);
                node.setSoilMoisture(68.0);
                node.setTiltAngle(7.4);
                node.setRainDrops(54);
                node.setSound(12.0);
                node.setVibrations(28.0);
                node.setTemp(24.5);
                node.setHumidity(82.0);
                node.setActive(true);
                node.setLocalDateTime(LocalDateTime.now());
                node = nodeRepository.save(node);

                for (int offset = 5; offset >= 0; offset--) {
                    NodeReadings reading = new NodeReadings();
                    reading.setNode(node);
                    reading.setSoilMoisture(60.0 + offset);
                    reading.setTiltAngle(5.0 + offset * 0.4);
                    reading.setRainDrops(42 + offset * 2);
                    reading.setSound(9.0 + offset * 0.5);
                    reading.setVibrations(20.0 + offset * 1.5);
                    reading.setTemp(22.0 + offset * 0.5);
                    reading.setHumidity(76.0 + offset);
                    reading.setLocalDateTime(LocalDateTime.now().minusMinutes(offset * 15L));
                    nodeHistoryRepository.save(reading);
                }
            }

            if (roadSegmentRepository.count() == 0) {
                RoadSegment segment = new RoadSegment();
                segment.setRoadName("NH-07 Tehri Ridge");
                segment.setStartLatitude(30.3782);
                segment.setStartLongitude(78.4804);
                segment.setEndLatitude(30.3910);
                segment.setEndLongitude(78.4950);
                segment.setGeographicalLocation("Tehri Garhwal, Uttarakhand");
                segment.setRainfall(54.0);
                segment.setTerrainSusceptibility(48.0);
                segment.setRecentIncidents(1);
                segment.setSensorRisk(35.0);
                segment.setDataUpdatedAt(LocalDateTime.now());
                roadSegmentRepository.save(segment);
            }
        };
    }
}
