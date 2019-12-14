package pl.potat0x.potapaas.potapaasservice.security;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDto;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDtoBuilder;
import pl.potat0x.potapaas.potapaasservice.app.AppResponseDto;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.datastore.DatastoreRequestDto;
import pl.potat0x.potapaas.potapaasservice.datastore.DatastoreResponseDto;
import pl.potat0x.potapaas.potapaasservice.user.ChangePasswordRequestDto;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
@Sql(statements = {
        "insert into user_ (id, username, password, uuid) values(101, 'testuser1', 'testpassword1', 'def456') on conflict do nothing",
        "insert into user_ (id, username, password, uuid) values(102, 'testuser2', 'testpassword2', 'def789') on conflict do nothing"
})
public class AccessControlTest {

    private TestRestTemplate user1 = new TestRestTemplate();
    private TestRestTemplate user2 = new TestRestTemplate();

    @LocalServerPort
    private int port;

    @Before
    public void authorizeTestRestTemplate() {
        TestAuthUtils.authorizeTestRestTemplate(user1, loginUrl(), "testuser1", "testpassword1");
        TestAuthUtils.authorizeTestRestTemplate(user2, loginUrl(), "testuser2", "testpassword2");
    }

    @Test
    public void userEndpointTest() {
        String user2Url = potapaasUrl() + "/user/testuser2";
        String changeUser2PasswordUrl = potapaasUrl() + "/user/testuser2/password";
        ChangePasswordRequestDto changePasswordRequestDto = new ChangePasswordRequestDto("testpassword2", "New_testpassword2");

        assertThat(get(user1, user2Url)).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(post(user1, changeUser2PasswordUrl, changePasswordRequestDto)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post(user2, changeUser2PasswordUrl, changePasswordRequestDto)).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(delete(user1, user2Url)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete(user2, user2Url)).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void datastoreEndpointTest() {
        String datastoreUrl = potapaasUrl() + "/datastore";
        DatastoreRequestDto user2datastoreRequestDto = new DatastoreRequestDto("user2-datastore", "POSTGRESQL");
        ResponseEntity<DatastoreResponseDto> user2DatastoreResponse = user2.postForEntity(datastoreUrl, user2datastoreRequestDto, DatastoreResponseDto.class);
        assertThat(user2DatastoreResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String user2DatastoreUrl = datastoreUrl + "/" + user2DatastoreResponse.getBody().getUuid();

        assertThat(get(user1, user2DatastoreUrl)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(user2, user2DatastoreUrl)).isEqualTo(HttpStatus.OK);

        assertThat(user1.getForEntity(datastoreUrl, String.class).getBody()).doesNotContain("user2-datastore");
        assertThat(user2.getForEntity(datastoreUrl, String.class).getBody()).contains("user2-datastore");

        assertThat(delete(user1, user2DatastoreUrl)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete(user2, user2DatastoreUrl)).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void appEndpointTest() {
        AppRequestDto user2appRequestDto = validAppRequestDtoBuilder().withName("user2-app").build();
        String appEndpointUrl = potapaasUrl() + "/app";
        ResponseEntity<AppResponseDto> user2AppResponse = user2.postForEntity(appEndpointUrl, user2appRequestDto, AppResponseDto.class);
        assertThat(user2AppResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String user2AppUrl = appEndpointUrl + "/" + user2AppResponse.getBody().getAppUuid();
        assertThat(get(user1, user2AppUrl)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(user2, user2AppUrl)).isEqualTo(HttpStatus.OK);

        String user2AppLogsUrl = user2AppUrl + "/logs";
        assertThat(get(user1, user2AppLogsUrl)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(user2, user2AppLogsUrl)).isEqualTo(HttpStatus.OK);

        assertThat(user1.getForEntity(appEndpointUrl, String.class).getBody()).doesNotContain("user2-app");
        assertThat(user2.getForEntity(appEndpointUrl, String.class).getBody()).contains("user2-app");

        String user1DatastoreUuid = createDatastore(user1, "user1-datastore");
        String user2DatastoreUuid = createDatastore(user2, "user2-datastore");
        AppRequestDto user1AppRequestDtoWithUser2Datastore = validAppRequestDtoBuilder().withName("datastore-attack")
                .withDatastoreUuid(user2DatastoreUuid).build();

        AppRequestDto user2AppRequestDtoWithUser1Datastore = validAppRequestDtoBuilder().withName("datastore-attack")
                .withDatastoreUuid(user1DatastoreUuid).build();

        assertThat(post(user1, appEndpointUrl, user1AppRequestDtoWithUser2Datastore)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post(user1, appEndpointUrl, user2AppRequestDtoWithUser1Datastore)).isEqualTo(HttpStatus.CREATED);

        AppRequestDto user2AppWithUser1DatastoreRequestDto = validAppRequestDtoBuilder().withName("user2-app")
                .withDatastoreUuid(user1DatastoreUuid).build();
        AppRequestDto user2AppWithUser2DatastoreRequestDto = validAppRequestDtoBuilder().withName("user2-app")
                .withDatastoreUuid(user2DatastoreUuid).build();
        String redeployAppEndpointUrl = user2AppUrl + "/redeploy";

        assertThat(post(user1, redeployAppEndpointUrl, user2AppWithUser1DatastoreRequestDto)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post(user1, redeployAppEndpointUrl, user2AppWithUser2DatastoreRequestDto)).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(post(user2, redeployAppEndpointUrl, user2AppWithUser1DatastoreRequestDto)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post(user2, redeployAppEndpointUrl, user2AppWithUser2DatastoreRequestDto)).isEqualTo(HttpStatus.OK);

        assertThat(delete(user1, user2AppUrl)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete(user2, user2AppUrl)).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private AppRequestDtoBuilder validAppRequestDtoBuilder() {
        return new AppRequestDtoBuilder()
                .withName("security-test-app" + UUID.randomUUID())
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok");
    }

    private String createDatastore(TestRestTemplate datastoreOwnerTestRestTemplate, String name) {
        DatastoreRequestDto datastoreRequestDto = new DatastoreRequestDto(name, "POSTGRESQL");
        String datastoreUrl = potapaasUrl() + "/datastore";
        ResponseEntity<DatastoreResponseDto> createDatastoreResponse = datastoreOwnerTestRestTemplate.postForEntity(datastoreUrl, datastoreRequestDto, DatastoreResponseDto.class);
        assertThat(createDatastoreResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return createDatastoreResponse.getBody().getUuid();
    }

    private String potapaasUrl() {
        return "http://127.0.0.1:" + port;
    }

    private String loginUrl() {
        return "http://127.0.0.1:" + port + "/login";
    }

    private HttpStatus get(TestRestTemplate testRestTemplate, String url) {
        return testRestTemplate.getForEntity(url, String.class).getStatusCode();
    }

    private HttpStatus post(TestRestTemplate testRestTemplate, String url, Object requestBody) {
        return testRestTemplate.postForEntity(url, requestBody, String.class).getStatusCode();
    }

    private HttpStatus delete(TestRestTemplate testRestTemplate, String url) {
        return testRestTemplate.exchange(url, HttpMethod.DELETE, null, String.class).getStatusCode();
    }
}
