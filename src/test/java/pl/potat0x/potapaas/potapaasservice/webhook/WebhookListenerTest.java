package pl.potat0x.potapaas.potapaasservice.webhook;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.potat0x.potapaas.potapaasservice.app.AppFacade;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDto;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDtoBuilder;
import pl.potat0x.potapaas.potapaasservice.app.AppResponseDto;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class WebhookListenerTest {

    @Autowired
    private AppFacade appFacade;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    @Test
    public void shouldRedeployAppViaWebhook() throws InterruptedException {
        //given: deployed app (commit 1)
        AppResponseDto appResponseDto = createAppWithAutoDeployEnabled();
        String appUuid = appResponseDto.getAppUuid();
        String appUrl = "http://127.0.0.1:" + appResponseDto.getExposedPort();
        waitForAppStart();
        assertThat(testRestTemplate.getForEntity(appUrl, String.class).getBody()).isEqualTo("Commit 1");

        //when: webhook payload is received
        String webhookPayload = "{" +
                "\"ref\": \"refs/heads/webhook_push_event_nodejs\"" +
                "}";
        assertThat(testRestTemplate.postForEntity(webhookListenerUrl(appUuid), webhookPayload, String.class).getStatusCode().value()).isEqualTo(200);

        //then: latest commit should be deployed (commit 4)
        int currentAppPort = appFacade.getAppDetails(appUuid).get().getExposedPort();
        String currentAppUrl = "http://127.0.0.1:" + currentAppPort;
        waitForAppStart();
        assertThat(testRestTemplate.getForEntity(currentAppUrl, String.class).getBody()).isEqualTo("Commit 4");
    }

    @Test
    public void shouldNotRedeployAppViaWebhookWhenOtherBranchPushed() throws InterruptedException {
        //given: deployed app (commit 1)
        AppResponseDto appResponseDto = createAppWithAutoDeployEnabled();
        String appUuid = appResponseDto.getAppUuid();
        String appUrl = "http://127.0.0.1:" + appResponseDto.getExposedPort();
        waitForAppStart();
        assertThat(testRestTemplate.getForEntity(appUrl, String.class).getBody()).isEqualTo("Commit 1");

        //when: webhook payload is received
        String webhookPayload = "{" +
                "\"ref\": \"refs/heads/nodejs_test_ok\"" +
                "}";
        assertThat(testRestTemplate.postForEntity(webhookListenerUrl(appUuid), webhookPayload, String.class).getStatusCode().value()).isEqualTo(409);

        //then: latest commit should be deployed (commit 4)
        int currentAppPort = appFacade.getAppDetails(appUuid).get().getExposedPort();
        String currentAppUrl = "http://127.0.0.1:" + currentAppPort;
        waitForAppStart();
        assertThat(testRestTemplate.getForEntity(currentAppUrl, String.class).getBody()).isEqualTo("Commit 1");
    }

    private AppResponseDto createAppWithAutoDeployEnabled() {
        AppRequestDto appRequestDtoCommit1 = validAppRequestDtoBuilder()
                .withCommitHash("f4f0e2ed")
                .withAutodeployEnabled(true)
                .build();
        return appFacade.createAndDeployApp(appRequestDtoCommit1).get();
    }

    private String potapaasUrl() {
        return "http://127.0.0.1:" + port;
    }

    private String webhookListenerUrl(String appUuid) {
        return potapaasUrl() + "/potapaas-push-event-listener/" + appUuid;
    }

    private AppRequestDtoBuilder validAppRequestDtoBuilder() {
        return new AppRequestDtoBuilder()
                .withName("webhook-test-app")
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("webhook_push_event_nodejs");
    }

    private void waitForAppStart() throws InterruptedException {
        Thread.sleep(PotapaasConfig.getInt("app_startup_waiting_time_in_millis"));
    }
}