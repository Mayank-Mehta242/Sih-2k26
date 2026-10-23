package Land.Slide.Early.Warn.system.LandSlidebyMayank.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @Value("${admin.phone-number}")
    private String adminPhoneNumber;

    public String sendAdminSms(String message) {
        if (isBlank(accountSid) || isBlank(authToken) || isBlank(twilioPhoneNumber)
                || isBlank(adminPhoneNumber)) {
            return "SMS not sent: Twilio and admin phone settings are not configured.";
        }

        try {
            Twilio.init(accountSid, authToken);
            Message sms = Message.creator(
                    new PhoneNumber(adminPhoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    message
            ).create();
            return "SMS sent successfully. Message SID: " + sms.getSid();
        } catch (Exception exception) {
            return "SMS failed: " + exception.getMessage();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
