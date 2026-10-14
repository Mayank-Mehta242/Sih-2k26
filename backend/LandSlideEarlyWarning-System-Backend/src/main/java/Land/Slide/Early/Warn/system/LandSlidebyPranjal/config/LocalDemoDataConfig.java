package Land.Slide.Early.Warn.system.LandSlidebyPranjal.config;

import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity.Node;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity.NodeReadings;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Repository.NodeHistoryRepository;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Repository.NodeRepository;
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
            NodeHistoryRepository nodeHistoryRepository
    ) {
        return args -> {
            if (nodeRepository.count() > 0) {
                return;
            }

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
        };
    }
}
