package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.control.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.potat0x.potapaas.potapaasservice.api.UuidAndNameResponseDto;
import pl.potat0x.potapaas.potapaasservice.security.TestAuthUtils;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class AppFacadeTest {

    @Autowired
    private AppFacade appFacade;

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    private String testString = "LowercaseAndUppercaseTest";

    @Before
    public void setUpSpringSecurityAuthentication() {
        TestAuthUtils.setAuthenticatedPrincipalInSecurityContext("app-facade-test-user", 456L);
    }

    @Test
    public void shouldRedeployApp() {
        //given
        AppResponseDto initialDeployment = deployTolowerApp();
        String appUuid = initialDeployment.getAppUuid();
        AppResponseDtoBuilder expectedResponse = appResponseDtoToBuilder(initialDeployment);
        assertThat(expectedResponse.build()).isEqualToIgnoringGivenFields(initialDeployment, "webhookSecret");
        assertThat(initialDeployment.getWebhookSecret()).isNotBlank();
        assertThat(checkIfLowercaseAppWorking(initialDeployment.getExposedPort())).isTrue();

        //when
        AppRequestDtoBuilder validRedeployAppRequestDto = validAppRequestDtoBuilder()
                .withName("toupper-app")
                .withSourceBranchName("nodejs_toupper")
                .withAutodeployEnabled(true);

        AppResponseDto expectedResponseAfterRedeployment = expectedResponse
                .withName("toupper-app")
                .withSourceBranchName("nodejs_toupper")
                .withCommitHash("558a76caf4b6bd1f9ccd65043423930dc54ca3d2")
                .withAutodeployEnabled(true)
                .withWebhookSecret(initialDeployment.getWebhookSecret())
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
        final String commitToDeploy3ExpectedHash = "8b887b23ad79f23c288b20b338de5b8b08e7acce";
        String logExpectedInTest3 = "DEPLOY_COMMIT_TEST_3";

        //when
        AppRequestDtoBuilder requestDto = validAppRequestDtoBuilder()
                .withSourceBranchName(branchName)
                .withCommitHash(commitToDeploy1);

        AppResponseDto appResponseDto = appFacade.createAndDeployApp(requestDto.build()).get();
        String appUuid = appResponseDto.getAppUuid();
        waitForAppStart();

        //then
        assertThat(appResponseDto.getCommitHash()).startsWith(commitToDeploy1);
        assertThat(appFacade.getAppDetails(appUuid).get().getCommitHash()).startsWith(commitToDeploy1);
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
        assertThat(redeployResponseDto.getCommitHash()).startsWith(commitToDeploy2);
        assertThat(appFacade.getAppDetails(appUuid).get().getCommitHash()).startsWith(commitToDeploy2);
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
        assertThat(redeployWithNoCommitSpecifiedResponseDto.getCommitHash()).isEqualTo(commitToDeploy3ExpectedHash);
        assertThat(appFacade.getAppDetails(appUuid).get().getCommitHash()).isEqualTo(commitToDeploy3ExpectedHash);
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
        assertThat(expectedResponse.build()).isEqualToIgnoringGivenFields(initialDeployment, "webhookSecret");
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

    @Test
    public void shouldNotAllowToDeployTwoAppsWithSameName() {
        //given
        AppResponseDto app1 = deployExampleApp();
        AppResponseDto app2 = deployExampleApp();
        AppRequestDto redeployRequestDtoWithNameOfApp1 = validAppRequestDtoBuilder()
                .withName(app1.getName())
                .build();

        //when: deploy new app with same name as app1
        Either<ErrorMessage, AppResponseDto> deployNewAppResponse = appFacade.createAndDeployApp(redeployRequestDtoWithNameOfApp1);
        //then
        assertThat(deployNewAppResponse.getLeft().getHttpStatus()).isEqualTo(409);
        assertThat(appFacade.getUuidsAndNamesOfAllApps().get()).contains(new UuidAndNameResponseDto(app1.getAppUuid(), app1.getName()));

        //when: redeploy app2 with name same as app1
        Either<ErrorMessage, AppResponseDto> app2redeployResponse = appFacade.redeployApp(app2.getAppUuid(), redeployRequestDtoWithNameOfApp1);
        //then
        assertThat(app2redeployResponse.getLeft().getHttpStatus()).isEqualTo(409);

        //when: redeploy app1 without changing name
        Either<ErrorMessage, AppResponseDto> app1RedeployResponse = appFacade.redeployApp(app1.getAppUuid(), redeployRequestDtoWithNameOfApp1);
        //then
        assertThat(app1RedeployResponse.get()).isEqualToIgnoringGivenFields(app1, "exposedPort");
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
                .withName("tolower-app" + UUID.randomUUID())
                .withSourceBranchName("nodejs_tolower")
                .withCommitHash("016a9fb7a6165d693aaafa1d3164474b564b46e9");
        return appFacade.createAndDeployApp(requestDto.build()).get();
    }

    private AppResponseDto deployExampleApp() {
        AppRequestDtoBuilder requestDto = validAppRequestDtoBuilder()
                .withName("facade-test-app" + UUID.randomUUID());
        return appFacade.createAndDeployApp(requestDto.build()).get();
    }

    private AppResponseDtoBuilder appResponseDtoToBuilder(AppResponseDto responseDto) {
        return new AppResponseDtoBuilder()
                .withAppUuid(responseDto.getAppUuid())
                .withName(responseDto.getName())
                .withType(responseDto.getType())
                .withSourceRepoUrl(responseDto.getSourceRepoUrl())
                .withSourceBranchName(responseDto.getSourceBranchName())
                .withCommitHash(responseDto.getCommitHash())
                .withCreatedAt(responseDto.getCreatedAt())
                .withStatus(responseDto.getStatus())
                .withExposedPort(responseDto.getExposedPort());
    }

    private AppRequestDtoBuilder validAppRequestDtoBuilder() {
        return new AppRequestDtoBuilder()
                .withName("app-name")
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok")
                .withAutodeployEnabled(false);
    }
}