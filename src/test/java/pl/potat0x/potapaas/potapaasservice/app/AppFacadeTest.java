package pl.potat0x.potapaas.potapaasservice.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class AppFacadeTest {

    @Autowired
    private AppFacade appFacade;
    @Autowired
    private TestRestTemplate testRestTemplate;

    private String testString = "LowercaseAndUppercaseTest";

    @Test
    public void shouldRedeployApp() {
        //given
        AppResponseDto initialDeployment = deployTolowerApp();
        String appUuid = initialDeployment.getAppUuid();
        AppResponseDtoBuilder expectedResponse = appResponseDtoToBuilder(initialDeployment);
        assertThat(expectedResponse.build()).isEqualTo(initialDeployment);
        assertThat(checkIfLowercaseAppWorking(initialDeployment.getExposedPort())).isTrue();

        //when
        AppRequestDtoBuilder validRedeployAppRequestDto = validAppRequestDtoBuilder()
                .withName("toupper-app")
                .withSourceBranchName("nodejs_toupper");

        AppResponseDto expectedResponseAfterRedeployment = expectedResponse
                .withName("toupper-app")
                .withSourceBranchName("nodejs_toupper")
                .build();

        AppResponseDto appRedeploymentResponseDto = appFacade.redeployApp(appUuid, validRedeployAppRequestDto.build()).get();

        //then
        assertThat(appRedeploymentResponseDto).isEqualToIgnoringGivenFields(expectedResponseAfterRedeployment, "exposedPort");
        assertThat(checkIfUppercaseAppWorking(appRedeploymentResponseDto.getExposedPort())).isTrue();
    }

    @Test
    public void shouldDeployAndRedeployAppWithGivenCommitHash() throws InterruptedException {
        //given
        String branchName = "nodejs_deploy_commit";
        String commitToDeploy1 = "2fb44c0";
        String logExpectedInTest1 = "DEPLOY_COMMIT_TEST_1";
        String commitToDeploy2 = "50df21e";
        String logExpectedInTest2 = "DEPLOY_COMMIT_TEST_2";
        final String commitToDeploy3 = null;
        String logExpectedInTest3 = "DEPLOY_COMMIT_TEST_3";

        //when
        AppRequestDtoBuilder requestDto = validAppRequestDtoBuilder()
                .withSourceBranchName(branchName)
                .withCommitHash(commitToDeploy1);

        AppResponseDto appResponseDto = appFacade.createAndDeployApp(requestDto.build()).get();
        String appUuid = appResponseDto.getAppUuid();
        waitForAppStart();

        //then
        assertThat(appResponseDto.getCommitHash()).isEqualTo(commitToDeploy1);
        assertThat(appFacade.getAppDetails(appUuid).get().getCommitHash()).isEqualTo(commitToDeploy1);
        assertThat(appFacade.getAppLogs(appUuid).get()).contains(logExpectedInTest1);
        assertThat(appFacade.getAppLogs(appUuid).get()).doesNotContain(logExpectedInTest2);
        assertThat(appFacade.getAppLogs(appUuid).get()).doesNotContain(logExpectedInTest3);


        //when
        AppRequestDtoBuilder redeployRequestDto = validAppRequestDtoBuilder()
                .withSourceBranchName(branchName)
                .withCommitHash(commitToDeploy2);

        AppResponseDto redeployResponseDto = appFacade.redeployApp(appUuid, redeployRequestDto.build()).get();
        waitForAppStart();

        //then
        assertThat(redeployResponseDto.getCommitHash()).isEqualTo(commitToDeploy2);
        assertThat(appFacade.getAppDetails(appUuid).get().getCommitHash()).isEqualTo(commitToDeploy2);
        assertThat(appFacade.getAppLogs(appUuid).get()).contains(logExpectedInTest2);
        assertThat(appFacade.getAppLogs(appUuid).get()).doesNotContain(logExpectedInTest1);
        assertThat(appFacade.getAppLogs(appUuid).get()).doesNotContain(logExpectedInTest3);


        //when
        AppRequestDtoBuilder redeployRequestDtoWithNoCommitSpecified = validAppRequestDtoBuilder()
                .withSourceBranchName(branchName)
                .withCommitHash(commitToDeploy3);

        AppResponseDto redeployWithNoCommitSpecifiedResponseDto = appFacade.redeployApp(appUuid, redeployRequestDtoWithNoCommitSpecified.build()).get();
        waitForAppStart();

        //then
        assertThat(redeployWithNoCommitSpecifiedResponseDto.getCommitHash()).isEqualTo(commitToDeploy3);
        assertThat(appFacade.getAppDetails(appUuid).get().getCommitHash()).isEqualTo(commitToDeploy3);
        assertThat(appFacade.getAppLogs(appUuid).get()).contains(logExpectedInTest3);
        assertThat(appFacade.getAppLogs(appUuid).get()).doesNotContain(logExpectedInTest1);
        assertThat(appFacade.getAppLogs(appUuid).get()).doesNotContain(logExpectedInTest2);
    }

    @Test
    public void shouldKeepCurrentDeploymentAliveAndUnmodifiedWhenRedeploymentFail() {
        //given
        AppResponseDto initialDeployment = deployTolowerApp();
        String appUuid = initialDeployment.getAppUuid();
        AppResponseDtoBuilder expectedResponse = appResponseDtoToBuilder(initialDeployment);
        assertThat(expectedResponse.build()).isEqualTo(initialDeployment);
        assertThat(checkIfLowercaseAppWorking(initialDeployment.getExposedPort())).isTrue();

        //when
        AppRequestDtoBuilder validRedeployAppRequestDto = validAppRequestDtoBuilder()
                .withName("new-name")
                .withSourceBranchName("nodejs_test_fail");

        ErrorMessage redeploymentError = appFacade.redeployApp(appUuid, validRedeployAppRequestDto.build()).getLeft();
        AppResponseDto deploymentAfterTestsFailure = appFacade.getAppDetails(appUuid).get();

        //then
        assertThat(redeploymentError.getText().toLowerCase()).contains("tests failed");
        assertThat(deploymentAfterTestsFailure).isEqualTo(initialDeployment);
        assertThat(checkIfLowercaseAppWorking(initialDeployment.getExposedPort())).isTrue();
    }

    private void waitForAppStart() throws InterruptedException {
        Thread.sleep(PotapaasConfig.getInt("app_startup_waiting_time_in_millis"));
    }

    private boolean checkIfUppercaseAppWorking(int appPort) {
        return testString.toUpperCase().equals(requestForChangeStringCase(appPort, testString));
    }

    private boolean checkIfLowercaseAppWorking(int appPort) {
        return testString.toLowerCase().equals(requestForChangeStringCase(appPort, testString));
    }

    private String requestForChangeStringCase(int appPort, String testString) {
        String appUrl = "http://127.0.0.1:" + appPort + "/";
        try {
            waitForAppStart();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ResponseEntity<String> appResponse = testRestTemplate.getForEntity(appUrl + testString, String.class);
        return appResponse.getBody();
    }

    private AppResponseDto deployTolowerApp() {
        AppRequestDtoBuilder requestDto = validAppRequestDtoBuilder()
                .withName("tolower-app")
                .withSourceBranchName("nodejs_tolower");
        return appFacade.createAndDeployApp(requestDto.build()).get();
    }

    private AppResponseDtoBuilder appResponseDtoToBuilder(AppResponseDto responseDto) {
        return new AppResponseDtoBuilder()
                .withAppUuid(responseDto.getAppUuid())
                .withName(responseDto.getName())
                .withType(responseDto.getType())
                .withSourceRepoUrl(responseDto.getSourceRepoUrl())
                .withSourceBranchName(responseDto.getSourceBranchName())
                .withCreatedAt(responseDto.getCreatedAt())
                .withStatus(responseDto.getStatus())
                .withExposedPort(responseDto.getExposedPort());
    }

    private AppRequestDtoBuilder validAppRequestDtoBuilder() {
        return new AppRequestDtoBuilder()
                .withName("app-name")
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok");
    }
}