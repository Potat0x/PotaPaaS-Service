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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class AppControllerTest {

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

    private AppResponseDtoBuilder validAppResponseDtoBuilder() {
        return new AppResponseDtoBuilder()
                .withName("app-name-test456")
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok")
                .withCreatedAt(LocalDateTime.now())
                .withStatus("running")
                .withExposedPort(32323);
    }

    private String endpointUrl() {
        return "http://127.0.0.1:" + port + "/app";
    }
}
