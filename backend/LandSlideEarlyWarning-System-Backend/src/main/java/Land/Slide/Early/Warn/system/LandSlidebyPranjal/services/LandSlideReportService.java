package Land.Slide.Early.Warn.system.LandSlidebyPranjal.services;

import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.LandSlideReportDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity.LandSlideReports;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Error.BadRequestException;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Repository.LandSlideReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
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

@RequiredArgsConstructor
@Service
public class LandSlideReportService {

    private final LandSlideReportRepository landSlideReportRepository;
    private final CloudinaryService cloudinaryService;
    private final RestTemplate restTemplate;

    private static final String MODEL_API =
            "https://user-camera-model-api-main.onrender.com/predict-disaster";

    private static final double MIN_CONFIDENCE = 0.60;

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

            ResponseEntity<Map> response = restTemplate.exchange(
                    MODEL_API,
                    HttpMethod.POST,
                    request,
                    Map.class
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

            if (!landslide) {
                throw new BadRequestException(
                        "Report rejected: the AI model did not detect a landslide in this image."
                );
            }

            if (confidence.doubleValue() < MIN_CONFIDENCE) {
                throw new BadRequestException(
                        "Report rejected: landslide confidence ("
                                + String.format("%.2f", confidence.doubleValue() * 100)
                                + "%) is below the required "
                                + String.format("%.0f", MIN_CONFIDENCE * 100)
                                + "% threshold."
                );
            }

            String imageUrl = cloudinaryService.uploadImage(image);

            LandSlideReports report = new LandSlideReports();
            report.setName(landSlideReportDto.getName());
            report.setMobileNo(landSlideReportDto.getMobileNo());
            report.setDescription(landSlideReportDto.getDescription());
            report.setLatitude(landSlideReportDto.getLatitude());
            report.setLongitude(landSlideReportDto.getLongitude());
            report.setImageUrl(imageUrl);

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
}