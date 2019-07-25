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

        ResponseEntity<AppResponseDto> responseEntity = testRestTemplate.postForEntity(endpointUrl(), appRequestDto, AppResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void shouldDetectInvalidRequestBody() {
        AppRequestDto appRequestDto = validAppRequestDtoBuilder()
                .withName("")
                .build();

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(endpointUrl(), appRequestDto, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void shouldGetApp() {
        AppRequestDto requestDto = validAppRequestDtoBuilder().build();
        AppResponseDto facadeResponseBody = appFacade.createAndDeployApp(requestDto).get();
        String appUrl = endpointUrl() + "/" + facadeResponseBody.getAppUuid();

        ResponseEntity<AppResponseDto> controllerResponse = testRestTemplate.getForEntity(appUrl, AppResponseDto.class);

        assertThat(controllerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controllerResponse.getBody()).isEqualTo(facadeResponseBody);
    }

    @Test
    public void shouldDeleteApp() {
        String appUuid = appFacade.createAndDeployApp(validAppRequestDtoBuilder().build()).get().getAppUuid();
        String appUrl = endpointUrl() + "/" + appUuid;

        assertThat(testRestTemplate.getForEntity(appUrl, AppResponseDto.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        testRestTemplate.delete(appUrl);

        assertThat(testRestTemplate.getForEntity(appUrl, AppResponseDto.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldNotCreateAppDueToInvalidUrl() {
        AppRequestDto appRequestDto = validAppRequestDtoBuilder()
                .withSourceRepoUrl("this is invalid url")
                .build();

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(endpointUrl(), appRequestDto, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private AppRequestDtoBuilder validAppRequestDtoBuilder() {
        return new AppRequestDtoBuilder()
                .withName("app-name-test123")
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok");
    }

    private String endpointUrl() {
        return "http://127.0.0.1:" + port + "/app";
    }
}
