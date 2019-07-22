package pl.potat0x.potapaas.potapaasservice.app;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
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
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;

import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("stubbed_docker_api")
public class AppControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(getDockerStubbedApiUrl());

    @Before
    public void prepareDockerStub() {

        stubFor(post(urlPathEqualTo("/build"))
                .willReturn(aResponse()
                        .withBodyFile("POST_build_test.json")
                )
        );

        stubFor(post(urlPathEqualTo("/containers/create"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("POST_containers_create_test.json")
                )
        );

        stubFor(post(urlPathMatching("/containers/[a-fA-F0-9]{64}/start"))
                .willReturn(aResponse().withStatus(204))
        );

        stubFor(get(urlPathMatching("/containers/[a-fA-F0-9]{64}/json"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("GET_containers_json.json")
                )
        );
    }

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

    private int getDockerStubbedApiUrl() {
        return Integer.parseInt(PotapaasConfig.get("docker_stubbed_api_uri").split(":")[2]);
    }
}
