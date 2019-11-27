package pl.potat0x.potapaas.potapaasservice;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.potat0x.potapaas.potapaasservice.security.Principal;

import java.util.Collections;
import java.util.List;

public final class TestAuthUtils {

    public static void authorizeTestRestTemplate(TestRestTemplate testRestTemplate, String loginUrl, String username, String password) {
        setAuthHeader(testRestTemplate, getAuthToken(loginUrl, username, password));
    }

    static String getAuthToken(String loginUrl, String username, String password) {

        String loginRequestBody = "{" +
                "\"username\": \"" + username + "\"," +
                "\"password\": \"" + password + "\"" +
                "}";

        ResponseEntity<String> loginResponse = new TestRestTemplate().postForEntity(loginUrl, loginRequestBody, String.class);
        List<String> authHeaders = loginResponse.getHeaders().get("Authorization");
        if (loginResponse.getStatusCode() != HttpStatus.OK || authHeaders == null || authHeaders.isEmpty()) {
            String errorMessage = TestAuthUtils.class.getSimpleName() + ": authorization failed: " +
                    "loginUrl: " + loginUrl +
                    " requestBody: " + loginRequestBody +
                    " responseStatus: " + loginResponse.getStatusCode();
            throw new RuntimeException(errorMessage);
        }
        return loginResponse.getHeaders().get("Authorization").get(0);
    }

    static void setAuthHeader(TestRestTemplate testRestTemplate, String authToken) {
        testRestTemplate.getRestTemplate().setInterceptors(List.of((request, body, execution) -> {
            request.getHeaders().add("Authorization", authToken);
            return execution.execute(request, body);
        }));
    }
}
