package pl.potat0x.potapaas.potapaasservice.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.potat0x.potapaas.potapaasservice.api.ResponseResolver;
import pl.potat0x.potapaas.potapaasservice.app.AppResponseDto;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.io.IOException;
import java.util.List;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@RestController
@RequestMapping("/potapaas-push-event-listener")
class WebhookListener {

    @Autowired
    private WebhookFacade webhookFacade;

    @Autowired
    WebhookListener(WebhookFacade webhookFacade) {
        this.webhookFacade = webhookFacade;
    }

    @PostMapping("/{appUuid}")
    ResponseEntity redeployApp(HttpEntity<String> httpEntity, @PathVariable String appUuid) {
        System.out.println("Received webhook: " + appUuid + "\nheaders: " + httpEntity.getHeaders() + "\npayload" + httpEntity.getBody());

        String eventSourceBranch;
        try {
            eventSourceBranch = readEventSourceBranch(httpEntity);
        } catch (Exception e) {
            System.out.println("Invalid payload JSON: " + e.getMessage());
            return ResponseResolver.toErrorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        System.out.println("Webhook source branch: " + eventSourceBranch + " (" + eventSourceBranch + ")");

        Either<ErrorMessage, String> hexDigest = readHmacDigestHeader(httpEntity)
                .toEither(message("X-Hub-Signature header not specified. See https://developer.github.com/webhooks/#delivery-headers", 403));

        Either<ErrorMessage, AppResponseDto> appResponseDto = hexDigest.map(digestX -> new HmacVerifier(httpEntity.getBody(), digestX))
                .flatMap(hmacVerifier -> webhookFacade.handleWebhook(appUuid, eventSourceBranch, hmacVerifier));

        return ResponseResolver.toResponseEntity(appResponseDto, HttpStatus.OK);
    }

    private Option<String> readHmacDigestHeader(HttpEntity<String> httpEntity) {
        List<String> responseHmacDigest = httpEntity.getHeaders().get("X-Hub-Signature");
        if (responseHmacDigest == null || responseHmacDigest.isEmpty()) {
            return Option.none();
        }
        return Option.of(responseHmacDigest.get(0));
    }

    private String readEventSourceBranch(HttpEntity<String> httpEntity) throws IOException {
        String json = httpEntity.getBody();
        JsonNode parent = new ObjectMapper().readTree(json);
        String refPropertyValue = parent.path("ref").asText();
        return parseEventSourceBranch(refPropertyValue);
    }

    private String parseEventSourceBranch(String refPropertyValue) {
        return refPropertyValue.substring(refPropertyValue.lastIndexOf("/") + 1);
    }
}
