package pl.potat0x.potapaas.potapaasservice.webhook

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import pl.potat0x.potapaas.potapaasservice.PotapaasServiceApplication
import pl.potat0x.potapaas.potapaasservice.app.AppFacade
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDto
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDtoBuilder
import pl.potat0x.potapaas.potapaasservice.app.AppResponseDto
import pl.potat0x.potapaas.potapaasservice.core.AppType
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig
import spock.lang.Specification

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
        assertThat(testRestTemplate.postForEntity(webhookListenerUrl(appUuid), webhookPayload, String.class).getStatusCode().value()).isEqualTo(expectedWebhookStatusCode)

        then: "latest commit should be deployed (commit 4) if autodeploy is enabled and app branch name is the same as in payload"
        waitForAppStart()
        String currentAppUrl = "http://127.0.0.1:" + appFacade.getAppDetails(appUuid).get().getExposedPort()
        assertThat(testRestTemplate.getForEntity(currentAppUrl, String.class).getBody()).isEqualTo(expectedAppResponse)

        where:
        autodeployEnabled | webhookPayload                                      | expectedWebhookStatusCode | expectedAppResponse
        true              | '{ "ref": "refs/heads/webhook_push_event_nodejs" }' | 200                       | "Commit 4"
        false             | '{ "ref": "refs/heads/webhook_push_event_nodejs" }' | 409                       | "Commit 1"
        true              | '{ "ref": "refs/heads/nodejs_test_ok" }'            | 409                       | "Commit 1"
    }

    def "should return bad request"() {
        expect: "webhook payload is received"
        assertThat(testRestTemplate.postForEntity(webhookListenerUrl(appUuid), webhookPayload, String.class).getStatusCode().value()).isEqualTo(expectedWebhookStatusCode)

        where:
        appUuid        | webhookPayload                                      | expectedWebhookStatusCode
        validUuid      | '{}'                                                | 400
        validUuid      | 'asd'                                               | 400
        validUuid      | ''                                                  | 400
        "invalid uuid" | '{ "ref": "refs/heads/webhook_push_event_nodejs" }' | 400
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
