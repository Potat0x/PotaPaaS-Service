package pl.potat0x.potapaas.potapaasservice.util;

import io.vavr.control.Either;
import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class AppDeploymentTest {

    @Test
    public void shouldDeployWebAppFromGithub() throws InterruptedException {
        AppDeployment app = appDeploymentFromTestGithubRepo("nodejs_test_ok_delay");
        Either<String, String> deploymentResult = app.deploy();

        String appUrl = "http://127.0.0.1:" + app.getPort().get();

        assertThat(deploymentResult.isRight()).isTrue();
        Thread.sleep(1000);
        ResponseEntity<String> response = new TestRestTemplate().getForEntity(appUrl, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        app.killApp().get();
    }

    @Test
    public void shouldDetectThatTestsFail() {
        AppDeployment app = appDeploymentFromTestGithubRepo("nodejs_test_fail_delay");

        Either<String, String> deploymentResult = app.deploy();

        assertThat(deploymentResult.getLeft()).contains("Tests failed");
    }

    private AppDeployment appDeploymentFromTestGithubRepo(String branchName) {
        return new AppDeployment(AppDeployment.DeploymentType.NODEJS, "https://github.com/Potat0x/potapaas-test-cases", branchName);
    }
}
