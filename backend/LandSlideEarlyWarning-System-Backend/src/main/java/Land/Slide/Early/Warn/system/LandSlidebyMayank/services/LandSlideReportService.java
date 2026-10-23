package Land.Slide.Early.Warn.system.LandSlidebyMayank.services;

import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.LandSlideReportDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Entity.LandSlideReports;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Error.BadRequestException;
import Land.Slide.Early.Warn.system.LandSlidebyMayank.Repository.LandSlideReportRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class LandSlideReportService {

    private final LandSlideReportRepository landSlideReportRepository;
    private final CloudinaryService cloudinaryService;
    private final RestTemplate restTemplate;
    private final SmsService smsService;

    public LandSlideReportService(
            LandSlideReportRepository landSlideReportRepository,
            CloudinaryService cloudinaryService,
            RestTemplate restTemplate,
            SmsService smsService) {
        this.landSlideReportRepository = landSlideReportRepository;
        this.cloudinaryService = cloudinaryService;
        this.restTemplate = restTemplate;
        this.smsService = smsService;
    }

    private static final String MODEL_API =
            "https://user-camera-model-api-main.onrender.com/predict-disaster";

    public ResponseDto createReport(LandSlideReportDto landSlideReportDto) {

        MultipartFile image = landSlideReportDto.getImage();

        if (image == null || image.isEmpty()) {
            throw new BadRequestException("Image is not provided");
        }

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename() != null
                            ? image.getOriginalFilename()
                            : "upload.jpg";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", imageResource);

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    MODEL_API,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            Map<String, Object> aiResponse = response.getBody();

            if (aiResponse == null) {
                throw new RuntimeException("AI model returned empty response");
            }

            Boolean landslide = (Boolean) aiResponse.get("landslide");
            Number confidence = (Number) aiResponse.get("confidence");

            if (landslide == null || confidence == null) {
                throw new RuntimeException("Invalid response from AI model");
            }

            // AI screening is advisory. Uncertain images are sent to the admin
            // review queue instead of being rejected before human review.

            String imageUrl = cloudinaryService.uploadImage(image);

            LandSlideReports report = new LandSlideReports();
            report.setName(landSlideReportDto.getName());
            report.setMobileNo(landSlideReportDto.getMobileNo());
            report.setDescription(landSlideReportDto.getDescription());
            report.setLatitude(landSlideReportDto.getLatitude());
            report.setLongitude(landSlideReportDto.getLongitude());
            report.setImageUrl(imageUrl);
            report.setApprovalStatus("PENDING");
            report.setAiDetected(landslide);
            report.setAiConfidence(confidence.doubleValue());

            LandSlideReports saved = landSlideReportRepository.save(report);

            return new ResponseDto(
                    "Landslide report submitted successfully. Report ID: " + saved.getId()
            );

        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error while processing landslide report: " + e.getMessage(), e
            );
        }
    }

    public List<LandSlideReports> getAllReports() {
        return landSlideReportRepository.findAll();
    }

    public LandSlideReports approveReport(long id, String reviewedBy, String reviewNote) {
        LandSlideReports report = reviewReport(id, "APPROVED", reviewedBy, reviewNote);
        smsService.sendAdminSms(String.format(
                "Landslide incident #%d approved. Location: %.5f, %.5f. Description: %s",
                report.getId(), report.getLatitude(), report.getLongitude(),
                report.getDescription() == null ? "No description" : report.getDescription()));
        return report;
    }

    public LandSlideReports rejectReport(long id, String reviewedBy, String reviewNote) {
        return reviewReport(id, "REJECTED", reviewedBy, reviewNote);
    }

    private LandSlideReports reviewReport(long id, String status, String reviewedBy, String reviewNote) {
        LandSlideReports report = landSlideReportRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Report not found: " + id));
        report.setApprovalStatus(status);
        report.setReviewedBy(reviewedBy);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewNote(reviewNote);
        return landSlideReportRepository.save(report);
    }
}
