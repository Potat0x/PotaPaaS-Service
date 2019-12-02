package pl.potat0x.potapaas.potapaasservice.app;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.potat0x.potapaas.potapaasservice.TestAuthUtils;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class ReverseProxyText {

    @Autowired
    private AppFacade appFacade;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Before
    public void setUpSpringSecurityAuthentication() {
        TestAuthUtils.setAuthenticatedPrincipalInSecurityContext("reverse-proxy-test-user", 234);
    }

    @Test
    public void appShouldBeExposedViaReverseProxy() throws InterruptedException {
        //given
        String appName = "test-app" + UUID.randomUUID();
        AppRequestDtoBuilder appRequestDto = new AppRequestDtoBuilder()
                .withName(appName)
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok");

        //when
        appFacade.createAndDeployApp(appRequestDto.build()).get();
        waitUntilAppWillBeExposedViaReverseProxy();

        String appUrl = "http://" + appName + ".localhost";
        ResponseEntity<String> testAppResponse = testRestTemplate.getForEntity(appUrl, String.class);

        //then
        assertThat(testAppResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(testAppResponse.getBody()).isEqualTo("Hello World!");
    }

    private void waitUntilAppWillBeExposedViaReverseProxy() throws InterruptedException {
        Thread.sleep(PotapaasConfig.getInt("reverse_proxy_expose_waiting_time_in_millis"));
    }
}
