package Land.Slide.Early.Warn.system.LandSlidebyMayank.Controllers;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RoadRiskResponse;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.RiskFactor;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.RoadSegment;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Error.GlobalExceptionHandler;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.services.RoadRiskScoringService;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.services.RoadSegmentService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoadSegmentControllerTest {
    private final RoadSegmentService segmentService = mock(RoadSegmentService.class);
    private final RoadRiskScoringService riskService = mock(RoadRiskScoringService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new RoadSegmentController(segmentService, riskService)
    ).setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(new LocalValidatorFactoryBean())
            .build();

    @Test
    void returnsRiskForASegment() throws Exception {
        when(segmentService.findById(7L)).thenReturn(segment(7L));
        when(riskService.assess(any())).thenReturn(response(7L));

        mvc.perform(get("/road-segments/7/risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.segmentId").value(7))
                .andExpect(jsonPath("$.riskScore").value(68))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.assessmentTimestamp").exists())
                .andExpect(jsonPath("$.dataFreshness").value("FRESH"));
    }

    @Test
    void returnsRiskForAllRequestedRouteSegments() throws Exception {
        when(segmentService.findById(7L)).thenReturn(segment(7L));
        when(segmentService.findById(8L)).thenReturn(segment(8L));
        when(riskService.assessRoute(any())).thenReturn(List.of(response(7L), response(8L)));

        mvc.perform(post("/road-segments/risk/assess")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"segmentIds":[7,8]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].segmentId").value(7))
                .andExpect(jsonPath("$[1].segmentId").value(8));
    }

    @Test
    void rejectsAnEmptyRouteRequest() throws Exception {
        mvc.perform(post("/road-segments/risk/assess")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"segmentIds":[]}
                                """))
                .andExpect(status().isBadRequest());
    }

    private RoadSegment segment(Long id) {
        RoadSegment segment = new RoadSegment();
        segment.setId(id);
        segment.setRoadName("NH-07");
        segment.setStartLatitude(30.1);
        segment.setStartLongitude(78.1);
        segment.setEndLatitude(30.2);
        segment.setEndLongitude(78.2);
        return segment;
    }

    private RoadRiskResponse response(Long id) {
        return new RoadRiskResponse(id, 68, "HIGH", "COMPLETE",
                List.of(new RiskFactor("rainfall", 20.4, "Rainfall contribution.")),
                List.of(), LocalDateTime.now(), LocalDateTime.now(), "FRESH");
    }
}
