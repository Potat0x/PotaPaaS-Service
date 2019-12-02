package pl.potat0x.potapaas.potapaasservice.app;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import pl.potat0x.potapaas.potapaasservice.security.TestAuthUtils;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.datastore.DatastoreRequestDto;
import pl.potat0x.potapaas.potapaasservice.datastore.DatastoreResponseDto;
import pl.potat0x.potapaas.potapaasservice.datastore.DatastoreType;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.user.UserFacade;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
@Sql(statements = "insert into user_ (id, username, password, uuid) values(123, 'app-controller-testuser', 'testpassword', 'def456') on conflict do nothing")
public class AppControllerTest {

    @Autowired
    private AppFacade appFacade;

    @Autowired
    private UserFacade userFacade;

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    private int port;

    @Before
    public void addAuthTokenToTestRestTemplate() {
        String loginUrl = "http://127.0.0.1:" + port + "/login";
        TestAuthUtils.authorizeTestRestTemplate(testRestTemplate, loginUrl, "app-controller-testuser", "testpassword");
    }

    @Before
    public void setUpSpringSecurityAuthentication() {
        TestAuthUtils.setAuthenticatedPrincipalInSecurityContext("app-controller-testuser", 123);
    }

    @Test
    public void shouldCreateApp() {
        AppRequestDto appRequestDto = validAppRequestDtoBuilder().build();

        ResponseEntity<AppResponseDto> responseEntity = testRestTemplate.postForEntity(appUrl(), appRequestDto, AppResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void shouldDetectInvalidRequestBody() {
        AppRequestDto appRequestDto = validAppRequestDtoBuilder()
                .withName("")
                .build();

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(appUrl(), appRequestDto, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void shouldGetApp() {
        AppRequestDto requestDto = validAppRequestDtoBuilder().build();
        AppResponseDto facadeResponseBody = appFacade.createAndDeployApp(requestDto).get();
        String appUrl = appUrl() + "/" + facadeResponseBody.getAppUuid();

        ResponseEntity<AppResponseDto> controllerResponse = testRestTemplate.getForEntity(appUrl, AppResponseDto.class);

        assertThat(controllerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controllerResponse.getBody()).isEqualTo(facadeResponseBody);
    }

    @Test
    public void shouldDeleteApp() {
        String appUuid = appFacade.createAndDeployApp(validAppRequestDtoBuilder().build()).get().getAppUuid();
        String appUrl = appUrl() + "/" + appUuid;

        assertThat(testRestTemplate.getForEntity(appUrl, AppResponseDto.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        testRestTemplate.delete(appUrl);

        assertThat(testRestTemplate.getForEntity(appUrl, AppResponseDto.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldDeleteCrashedApp() {
        AppRequestDtoBuilder requestDto = validAppRequestDtoBuilder()
                .withSourceBranchName("nodejs_test_ok_start_fail");
        String appUuid = appFacade.createAndDeployApp(requestDto.build()).get().getAppUuid();
        String appUrl = appUrl() + "/" + appUuid;

        assertThat(testRestTemplate.getForEntity(appUrl, AppResponseDto.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        testRestTemplate.delete(appUrl);

        assertThat(testRestTemplate.getForEntity(appUrl, AppResponseDto.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldChangeWebhookSecret() {
        //given
        AppRequestDtoBuilder requestDto = validAppRequestDtoBuilder()
                .withName("tolower-app" + UUID.randomUUID())
                .withSourceBranchName("nodejs_tolower");
        AppResponseDto initialDeployment = appFacade.createAndDeployApp(requestDto.build()).get();
        String initialWebhookSecret = initialDeployment.getWebhookSecret();
        String urlToChangeSecret = appUrl() + "/" + initialDeployment.getAppUuid() + "/change-webhook-secret";

        //when secret is empty -> error should be returned
        String newSecret = "";
        ResponseEntity<AppResponseDto> changeSecretResponse = testRestTemplate.postForEntity(urlToChangeSecret, new WebhookSecretRequestDto(newSecret), AppResponseDto.class);

        //then
        assertThat(changeSecretResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(appFacade.getAppDetails(initialDeployment.getAppUuid()).get()).isEqualTo(initialDeployment);

        //when secret is not specified in request -> random secret should be set
        newSecret = null;
        changeSecretResponse = testRestTemplate.postForEntity(urlToChangeSecret, new WebhookSecretRequestDto(newSecret), AppResponseDto.class);
        AppResponseDto appWithNewWebhookSecret = changeSecretResponse.getBody();
        String webhookSecret = appWithNewWebhookSecret.getWebhookSecret();

        //then
        assertThat(initialWebhookSecret).isNotBlank();
        assertThat(changeSecretResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(appWithNewWebhookSecret).isEqualToIgnoringGivenFields(initialDeployment, "webhookSecret");
        assertThat(webhookSecret).isNotBlank();
        assertThat(webhookSecret).isNotEqualTo(initialWebhookSecret);

        //when secret is specified in request -> set it
        newSecret = "new-secret";
        changeSecretResponse = testRestTemplate.postForEntity(urlToChangeSecret, new WebhookSecretRequestDto(newSecret), AppResponseDto.class);
        appWithNewWebhookSecret = changeSecretResponse.getBody();
        webhookSecret = appWithNewWebhookSecret.getWebhookSecret();

        //then
        assertThat(webhookSecret).isEqualTo("new-secret");
        assertThat(changeSecretResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(appWithNewWebhookSecret).isEqualToIgnoringGivenFields(initialDeployment, "webhookSecret");
    }

    @Test
    public void shouldNotCreateAppDueToInvalidUrl() {
        AppRequestDto appRequestDto = validAppRequestDtoBuilder()
                .withSourceRepoUrl("this is invalid url")
                .build();

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(appUrl(), appRequestDto, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void shouldDeployNewAppConnectedToPostgresDatastoreAndConnectItToAnotherPostgresDatastore() throws InterruptedException {
        String datastoreUuid = createDatastoreAndGetUuid(DatastoreType.POSTGRESQL);
        AppRequestDto appRequestDto = validAppRequestDtoBuilder()
                .withSourceBranchName("nodejs_postgres")
                .withDatastoreUuid(datastoreUuid)
                .build();

        ResponseEntity<AppResponseDto> responseEntity = testRestTemplate.postForEntity(appUrl(), appRequestDto, AppResponseDto.class);
        final String appUuid = responseEntity.getBody().getAppUuid();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().getDatastoreUuid()).isEqualTo(datastoreUuid);
        waitForAppStart();
        checkIfAppIsWorkingWithDatastore(responseEntity.getBody().getExposedPort());


        String newDatastoreUuid = createDatastoreAndGetUuid(DatastoreType.POSTGRESQL);
        AppRequestDto appRequestDtoWithNewDatastore = validAppRequestDtoBuilder()
                .withSourceBranchName("nodejs_postgres")
                .withDatastoreUuid(newDatastoreUuid)
                .build();

        responseEntity = testRestTemplate.postForEntity(redeployAppUrl(appUuid), appRequestDtoWithNewDatastore, AppResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getDatastoreUuid()).isEqualTo(newDatastoreUuid);
        waitForAppStart();
        checkIfAppIsWorkingWithDatastore(responseEntity.getBody().getExposedPort());
    }

    @Test
    public void shouldDeployNewAppConnectedToDatastore() throws InterruptedException {
        for (DatastoreType datastoreType : List.of(DatastoreType.MYSQL, DatastoreType.MARIADB)) {
            String datastoreUuid = createDatastoreAndGetUuid(datastoreType);
            AppRequestDto appRequestDto = validAppRequestDtoBuilder()
                    .withSourceBranchName("nodejs_mysql")
                    .withDatastoreUuid(datastoreUuid)
                    .build();

            ResponseEntity<AppResponseDto> responseEntity = testRestTemplate.postForEntity(appUrl(), appRequestDto, AppResponseDto.class);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(responseEntity.getBody().getDatastoreUuid()).isEqualTo(datastoreUuid);
            waitForAppStart();
            checkIfAppIsWorkingWithDatastore(responseEntity.getBody().getExposedPort());
        }
    }

    private void checkIfAppIsWorkingWithDatastore(int appPort) {
        //test table in datastore not initialized
        assertThat(readIterValueFromTestApp(appPort).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        //initialize test table
        assertThat(initIterValueInTestApp(appPort).getStatusCode()).isEqualTo(HttpStatus.OK);

        //read initialized value
        ResponseEntity<String> readIterResponse = readIterValueFromTestApp(appPort);
        assertThat(readIterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readIterResponse.getBody()).isEqualTo("0");

        //update value in app datastore
        incrementIterValueInTestApp(appPort);
        incrementIterValueInTestApp(appPort);

        //read updated value from datastore
        readIterResponse = readIterValueFromTestApp(appPort);
        assertThat(readIterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readIterResponse.getBody()).isEqualTo("2");
    }

    private String createDatastoreAndGetUuid(DatastoreType datastoreType) {
        DatastoreRequestDto datastoreRequestDto = new DatastoreRequestDto("test-datastore", datastoreType.toString());
        ResponseEntity<DatastoreResponseDto> datastoreResponseDtoResponseEntity = testRestTemplate.postForEntity(datastoreUrl(), datastoreRequestDto, DatastoreResponseDto.class);
        return datastoreResponseDtoResponseEntity.getBody().getUuid();
    }

    private AppRequestDtoBuilder validAppRequestDtoBuilder() {
        return new AppRequestDtoBuilder()
                .withName("app-name-test123" + UUID.randomUUID())
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok");
    }

    private ResponseEntity<String> readIterValueFromTestApp(int appPort) {
        return httpGetString(appPort, "/read-iter");
    }

    private ResponseEntity<String> incrementIterValueInTestApp(int appPort) {
        return httpGetString(appPort, "/increment-iter");
    }

    private ResponseEntity<String> initIterValueInTestApp(int appPort) {
        return httpGetString(appPort, "/init-iter");
    }

    private ResponseEntity<String> httpGetString(int appPort, String endpointUrl) {
        return testRestTemplate.getForEntity("http://127.0.0.1:" + appPort + endpointUrl, String.class);
    }

    private String redeployAppUrl(String appUuid) {
        return appUrl() + "/" + appUuid + "/redeploy";
    }

    private String appUrl() {
        return "http://127.0.0.1:" + port + "/app";
    }

    private String datastoreUrl() {
        return "http://127.0.0.1:" + port + "/datastore";
    }

    private void waitForAppStart() throws InterruptedException {
        Thread.sleep(PotapaasConfig.getInt("app_startup_waiting_time_in_millis"));
    }
}
