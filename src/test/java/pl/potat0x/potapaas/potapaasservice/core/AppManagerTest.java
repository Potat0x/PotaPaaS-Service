package pl.potat0x.potapaas.potapaasservice.core;

import io.vavr.control.Either;
import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDto;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDtoBuilder;
import pl.potat0x.potapaas.potapaasservice.config.AppManagerFactoryConfig;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import static org.assertj.core.api.Assertions.assertThat;

public class AppManagerTest {

    @Test
    public void shouldDeployWebAppFromGithub() throws InterruptedException {
        AppManager app = appManagerFromTestGithubRepo("nodejs_test_ok_delay", AppType.NODEJS);
        Either<ErrorMessage, String> deploymentResult = app.deploy();

        String appUrl = "http://127.0.0.1:" + app.getPort().get();

        assertThat(deploymentResult.isRight()).isTrue();
        Thread.sleep(PotapaasConfig.getInt("app_startup_waiting_time_in_millis"));
        ResponseEntity<String> response = new TestRestTemplate().getForEntity(appUrl, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        app.killApp().get();
    }

    @Test
    public void shouldDetectThatTestsFail() {
        AppManager app = appManagerFromTestGithubRepo("nodejs_test_fail_delay", AppType.NODEJS);

        Either<ErrorMessage, String> deploymentResult = app.deploy();

        assertThat(deploymentResult.getLeft().getText().toLowerCase()).contains("tests failed");
    }

    private AppManager appManagerFromTestGithubRepo(String branchName, AppType appType) {
        AppManagerFactory appManagerFactory = new AppManagerFactoryConfig().defaultAppManagerFactory();
        AppRequestDto appRequestDto = new AppRequestDtoBuilder()
                .withName("depl-test-app-name")
                .withType(appType.name())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName(branchName)
                .withAutodeployEnabled(false)
                .build();

        return appManagerFactory.forNewApp(appRequestDto);
    }
}
