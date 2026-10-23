package Land.Slide.Early.Warn.system.LandSlidebyMayank.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;
@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile image) throws Exception {

        // Local development can save a report without external image storage.
        // Production should configure Cloudinary so the photo is retained.
        if (cloudName == null || cloudName.isBlank()
                || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) {
            String contentType = image.getContentType() == null
                    ? "image/jpeg"
                    : image.getContentType();
            return "data:" + contentType + ";base64," +
                    Base64.getEncoder().encodeToString(image.getBytes());
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                image.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image"
                )
        );

        return result.get("secure_url").toString();
    }

}
