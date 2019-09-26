package pl.potat0x.potapaas.potapaasservice.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.datastore.DatastoreRequestDto;
import pl.potat0x.potapaas.potapaasservice.datastore.DatastoreResponseDto;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class AppControllerTest {

    @Autowired
    private AppFacade appFacade;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

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
    public void shouldNotCreateAppDueToInvalidUrl() {
        AppRequestDto appRequestDto = validAppRequestDtoBuilder()
                .withSourceRepoUrl("this is invalid url")
                .build();

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(appUrl(), appRequestDto, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void shouldDeployNewAppConnectedToDatastoreAndConnectItToAnotherDatastore() {
        String datastoreUuid = createDatastoreAndGetUuid();

        AppRequestDto appRequestDto = validAppRequestDtoBuilder()
                .withSourceBranchName("nodejs_postgres")
                .withDatastoreUuid(datastoreUuid)
                .build();

        ResponseEntity<AppResponseDto> responseEntity = testRestTemplate.postForEntity(appUrl(), appRequestDto, AppResponseDto.class);
        final String appUuid = responseEntity.getBody().getAppUuid();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        checkIfAppIsWorkingWithDatastore(responseEntity.getBody().getExposedPort());


        String newDatastoreUuid = createDatastoreAndGetUuid();
        AppRequestDto appRequestDtoWithNewDatastore = validAppRequestDtoBuilder()
                .withSourceBranchName("nodejs_postgres")
                .withDatastoreUuid(newDatastoreUuid)
                .build();

        responseEntity = testRestTemplate.postForEntity(redeployAppUrl(appUuid), appRequestDtoWithNewDatastore, AppResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        checkIfAppIsWorkingWithDatastore(responseEntity.getBody().getExposedPort());
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

    private String createDatastoreAndGetUuid() {
        DatastoreRequestDto datastoreRequestDto = new DatastoreRequestDto("test-datastore", "POSTGRES");
        ResponseEntity<DatastoreResponseDto> datastoreResponseDtoResponseEntity = testRestTemplate.postForEntity(datastoreUrl(), datastoreRequestDto, DatastoreResponseDto.class);
        return datastoreResponseDtoResponseEntity.getBody().getUuid();
    }

    private AppRequestDtoBuilder validAppRequestDtoBuilder() {
        return new AppRequestDtoBuilder()
                .withName("app-name-test123")
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok");
    }

    private ResponseEntity<String> readIterValueFromTestApp(int appPort) {
        return httpGetString(appPort, "/postgres-read-iter");
    }

    private ResponseEntity<String> incrementIterValueInTestApp(int appPort) {
        return httpGetString(appPort, "/postgres-increment-iter");
    }

    private ResponseEntity<String> initIterValueInTestApp(int appPort) {
        return httpGetString(appPort, "/postgres-init-iter");
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
}
