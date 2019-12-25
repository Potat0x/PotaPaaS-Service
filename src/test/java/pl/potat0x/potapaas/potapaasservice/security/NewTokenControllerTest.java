package pl.potat0x.potapaas.potapaasservice.security;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
@Sql(statements = {
        "insert into user_ (id, username, password, uuid) values(101, 'testuser1', 'testpassword1', 'def456') on conflict do nothing"
})
public class NewTokenControllerTest {

    @LocalServerPort
    private int port;

    @Test
    public void shouldGenerateNewAuthToken() throws IOException, InterruptedException {
        //given
        String authToken = TestAuthUtils.getAuthToken(loginUrl(), "testuser1", "testpassword1");

        //when
        HttpClient client = HttpClients.custom().build();
        HttpUriRequest newTokenRequest = RequestBuilder.get()
                .setUri(newTokenUrl())
                .setHeader("Authorization", authToken)
                .build();

        Thread.sleep(1500); //wait more than one second (for update JWT exp claim)
        HttpResponse newTokenResponse = client.execute(newTokenRequest);

        //then
        assertThat(newTokenResponse.getStatusLine().getStatusCode()).isEqualTo(200);
        String newAuthToken = newTokenResponse.getHeaders("Authorization")[0].getValue();
        assertThat(newAuthToken).isNotEqualTo(authToken);

        //check if value returned in auth header is valid JWT token and can be used to authorization
        HttpUriRequest tokenTestRequest = RequestBuilder.get()
                .setUri(datastoreUrl())
                .setHeader("Authorization", newAuthToken)
                .build();
        assertThat(client.execute(tokenTestRequest).getStatusLine().getStatusCode()).isEqualTo(200);
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    private String loginUrl() {
        return baseUrl() + "/login";
    }

    private String datastoreUrl() {
        return baseUrl() + "/datastore";
    }

    private String newTokenUrl() {
        return baseUrl() + "/new-auth-token";
    }
}