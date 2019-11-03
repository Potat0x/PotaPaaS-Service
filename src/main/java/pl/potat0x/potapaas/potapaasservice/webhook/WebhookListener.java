package pl.potat0x.potapaas.potapaasservice.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
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
    ResponseEntity redeployApp(HttpEntity<String> httpEntity, @PathVariable String appUuid) throws IOException {
        System.out.println("Received webhook: " + appUuid + "\nheaders: " + httpEntity.getHeaders() + "\npayload" + httpEntity.getBody());

        String eventSourceBranch = readEventSourceBranch(httpEntity);
        System.out.println("Webhook source branch: " + eventSourceBranch + " (" + eventSourceBranch + ")");

        Either<ErrorMessage, AppResponseDto> redeploymentResult = webhookFacade.handleWebhook(appUuid, eventSourceBranch);
        return ResponseResolver.toResponseEntity(redeploymentResult, HttpStatus.OK);
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
