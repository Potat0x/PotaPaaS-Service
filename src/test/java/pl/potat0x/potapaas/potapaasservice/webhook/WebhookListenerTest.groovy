package pl.potat0x.potapaas.potapaasservice.webhook

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.LinkedMultiValueMap
import pl.potat0x.potapaas.potapaasservice.PotapaasServiceApplication
import pl.potat0x.potapaas.potapaasservice.app.AppFacade
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDto
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDtoBuilder
import pl.potat0x.potapaas.potapaasservice.app.AppResponseDto
import pl.potat0x.potapaas.potapaasservice.core.AppType
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig
import spock.lang.Specification

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import static org.apache.commons.codec.binary.Hex.encodeHexString
import static org.assertj.core.api.Assertions.assertThat

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PotapaasServiceApplication.class])
@ActiveProfiles(profiles = ["test"])
class WebhookListenerTest extends Specification {

    @Autowired
    AppFacade appFacade

    @Autowired
    TestRestTemplate testRestTemplate

    @LocalServerPort
    int port

    static String validUuid = "00000000-0000-0000-0000-000000000000"

    def "should redeploy or not redeploy app when receiving valid payload"() {
        given: "deployed app (commit 1)"
        String appUuid = createAppWithAutoDeployEnabled(autodeployEnabled).getAppUuid()
        waitForAppStart()
        AppResponseDto appResponseDto = appFacade.getAppDetails(appUuid).get()
        String appUrl = "http://127.0.0.1:" + appResponseDto.getExposedPort()
        assertThat(testRestTemplate.getForEntity(appUrl, String.class).getBody()).isEqualTo("Commit 1")

        when: "webhook payload is received"
        def webhookSecret = appResponseDto.webhookSecret + (!useInvalidSecret ? "" : "_invalid_secret")
        assertThat(webhookRequest(appUuid, webhookPayload, webhookSecret).getStatusCodeValue()).isEqualTo(expectedWebhookStatusCode)

        then: "latest commit should be deployed (commit 4) if autodeploy is enabled and app branch name is the same as in payload"
        waitForAppStart()
        String currentAppUrl = "http://127.0.0.1:" + appFacade.getAppDetails(appUuid).get().getExposedPort()
        assertThat(testRestTemplate.getForEntity(currentAppUrl, String.class).getBody()).isEqualTo(expectedAppResponse)

        where:
        autodeployEnabled | webhookPayload                                      | useInvalidSecret | expectedWebhookStatusCode | expectedAppResponse
        true              | '{ "ref": "refs/heads/webhook_push_event_nodejs" }' | false            | 200                       | "Commit 4"
        true              | '{ "ref": "refs/heads/webhook_push_event_nodejs" }' | true             | 401                       | "Commit 1"
        false             | '{ "ref": "refs/heads/webhook_push_event_nodejs" }' | false            | 409                       | "Commit 1"
        true              | '{ "ref": "refs/heads/nodejs_test_ok" }'            | false            | 409                       | "Commit 1"
    }

    def "should return 400 error when request body or URL is invalid"() {
        expect: "webhook payload is received"
        webhookRequest(appUuid, webhookPayload, "dummy secret").getStatusCode().value() == expectedWebhookStatusCode

        where:
        appUuid        | webhookPayload                                      | expectedWebhookStatusCode
        validUuid      | '{}'                                                | 400
        validUuid      | '{ invalid json'                                    | 400
        validUuid      | ''                                                  | 400
        "invalid uuid" | '{ "ref": "refs/heads/webhook_push_event_nodejs" }' | 400
    }

    def "should return 403 when HMAC header not found"() {
        given:
        def webhookPayload = '{ "ref": "refs/heads/webhook_push_event_nodejs" }'

        expect:
        testRestTemplate.postForEntity(webhookListenerUrl(validUuid), webhookPayload, String.class).getStatusCode().value() == 403
    }

    private ResponseEntity webhookRequest(String appUuid, String body, String webhookSecret) {
        def headers = new LinkedMultiValueMap<String, String>()
        headers.add("X-Hub-Signature", calcHmacSha1HexDigest(body, webhookSecret))
        return testRestTemplate.exchange(webhookListenerUrl(appUuid), HttpMethod.POST, new HttpEntity<String>(body, headers), AppResponseDto.class)
    }

    private static String calcHmacSha1HexDigest(String message, String secret) {
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA1")
        Mac mac = Mac.getInstance("HmacSHA1")
        mac.init(keySpec)
        return encodeHexString(mac.doFinal(message.getBytes()))
    }

    private AppResponseDto createAppWithAutoDeployEnabled(boolean autodeployEnabled) {
        AppRequestDto appRequestDtoCommit1 = validAppRequestDtoBuilder()
                .withCommitHash("f4f0e2ed")
                .withAutodeployEnabled(autodeployEnabled)
                .build()
        return appFacade.createAndDeployApp(appRequestDtoCommit1).get()
    }

    private String potapaasUrl() {
        return "http://127.0.0.1:" + port
    }

    private String webhookListenerUrl(String appUuid) {
        return potapaasUrl() + "/potapaas-push-event-listener/" + appUuid
    }

    private static AppRequestDtoBuilder validAppRequestDtoBuilder() {
        return new AppRequestDtoBuilder()
                .withName("webhook-test-app")
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("webhook_push_event_nodejs")
    }

    private static void waitForAppStart() throws InterruptedException {
        Thread.sleep(PotapaasConfig.getInt("app_startup_waiting_time_in_millis"))
    }
}
