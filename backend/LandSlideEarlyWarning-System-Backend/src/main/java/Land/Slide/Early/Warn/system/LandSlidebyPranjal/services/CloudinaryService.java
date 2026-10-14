package Land.Slide.Early.Warn.system.LandSlidebyPranjal.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;
    public String uploadImage(MultipartFile image) throws Exception {

        Map<?, ?> result = cloudinary.uploader().upload(
                image.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image"
                )
        );

        return result.get("secure_url").toString();
    }

}
