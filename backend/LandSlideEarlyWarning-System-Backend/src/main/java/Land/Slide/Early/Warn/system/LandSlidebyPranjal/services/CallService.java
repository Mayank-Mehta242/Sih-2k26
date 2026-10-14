package Land.Slide.Early.Warn.system.LandSlidebyPranjal.services;

import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Dto.ResponseDto;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Entity.Node;
import Land.Slide.Early.Warn.system.LandSlidebyPranjal.Repository.NodeRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CallService {

    private final NodeRepository nodeRepository;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @Value("${admin.phone-number}")
    private String adminPhoneNumber;


    public ResponseDto makeCall(long id) {

        // Find node using ID
        Node node = nodeRepository.findById(id)
                .orElse(null);

        if (node == null) {
            return new ResponseDto(
                    "Node not found with id: " + id
            );
        }

        String nodeName = node.getNodeName();

        // Hindi alert message
        String message =
                "सावधान। " +
                        nodeName +
                        " क्षेत्र में भूस्खलन का जोखिम बहुत अधिक है। " +
                        "कृपया सावधानी बरतें और आसपास के क्षेत्र में चेतावनी प्रसारित करें।";

        // Twilio instructions
        String twiml =
                "<Response>" +
                        "<Say language=\"hi-IN\">" +
                        message +
                        "</Say>" +
                        "</Response>";

        try {

            Twilio.init(accountSid, authToken);

            // VOICE CALL
            Call call = Call.creator(
                    new PhoneNumber(adminPhoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    new Twiml(twiml)
            ).create();

            return new ResponseDto(
                    "Call initiated successfully. Call SID: "
                            + call.getSid()
            );

        } catch (Exception e) {

            return new ResponseDto(
                    "Failed to make call: " + e.getMessage()
            );
        }
    }
}