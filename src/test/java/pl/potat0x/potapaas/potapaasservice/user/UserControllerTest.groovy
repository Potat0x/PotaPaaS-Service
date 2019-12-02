package pl.potat0x.potapaas.potapaasservice.user

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import pl.potat0x.potapaas.potapaasservice.PotapaasServiceApplication
import pl.potat0x.potapaas.potapaasservice.security.TestAuthUtils
import spock.lang.Specification

import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PotapaasServiceApplication.class])
@ActiveProfiles(profiles = ["test"])
class UserControllerTest extends Specification {

    TestRestTemplate testRestTemplate

    @LocalServerPort
    int port

    def setup() {
        testRestTemplate = new TestRestTemplate()
    }

    def "user CRUD test"() {
        given:
        def username = "user123"
        def password = "Valid-Password!1"
        def validRequestDto = new UserRequestDto(username, password, "validemail@example.com")

        when:
        ResponseEntity<UserResponseDto> response = testRestTemplate.postForEntity(userEndpoint(), validRequestDto, UserResponseDto.class)
        authorizeTestRestTemplate(username, password)

        then:
        response.statusCodeValue == 201
        response.getBody().username == validRequestDto.getUsername()
        response.getBody().email == validRequestDto.getEmail()
        response.getBody().createdAt.isBefore(LocalDateTime.now())
        testRestTemplate.getForEntity(userEndpoint(username), UserResponseDto.class).getBody() == response.getBody()

        when: "create second user with same username as first user"
        ResponseEntity<UserResponseDto> response2 = testRestTemplate.postForEntity(userEndpoint(), validRequestDto, UserResponseDto.class)

        then: "user should not be created because username must be unique"
        response2.statusCodeValue == 409

        when:
        testRestTemplate.delete(userEndpoint(username))

        then:
        testRestTemplate.getForEntity(userEndpoint(username), UserResponseDto.class).statusCodeValue == 403
    }

    def "should return 422 while trying to create user with invalid DTO"() {
        given:
        def requestDto = new UserRequestDto(username, password, email)

        when:
        ResponseEntity<UserResponseDto> response = testRestTemplate.postForEntity(userEndpoint(), requestDto, UserResponseDto.class)

        then:
        response.statusCodeValue == 422
        testRestTemplate.getForEntity(userEndpoint(username), UserResponseDto.class).statusCodeValue == 403

        where:
        username             | password           | email
        "invalid username"   | "Valid-Password!1" | "validemail@example.com"
        null                 | "Valid-Password!1" | "validemail@example.com"
        "valid-username-123" | "invalid password" | "validemail@example.com"
        "valid-username-123" | null               | "validemail@example.com"
        "valid-username-123" | "Valid-Password!1" | "invalidemail@@example.com"
        "valid-username-123" | "Valid-Password!1" | null
    }

    def "change password test"() {
        given:
        def username = "user123"
        def initialPassword = "Initial-Valid-Password!1"
        def validRequestDto = new UserRequestDto(username, initialPassword, "validemail@example.com")
        testRestTemplate.postForEntity(potapaasUrl() + "/user", validRequestDto, UserResponseDto.class)
        authorizeTestRestTemplate(username, initialPassword)

        when: "invalid change password request: given current password is different than password database"
        def response1 = changePasswordRequest(username, new ChangePasswordRequestDto("123", "New-Valid-Password!1"))

        then:
        response1.statusCodeValue == 401

        when: "invalid change password request: new password is invalid"
        def response2 = changePasswordRequest(username, new ChangePasswordRequestDto(initialPassword, "12345678"))

        then:
        response2.statusCodeValue == 422

        when: "valid change password request"
        def response3 = changePasswordRequest(username, new ChangePasswordRequestDto(initialPassword, "New-Valid-Password!2"))

        then:
        response3.statusCodeValue == 204
    }

    private ResponseEntity<String> changePasswordRequest(String username, ChangePasswordRequestDto changePasswordRequestDto) {
        testRestTemplate.postForEntity(userEndpoint(username) + "/password", changePasswordRequestDto, String.class)
    }

    def authorizeTestRestTemplate(String username, String password) {
        String loginUrl = "http://127.0.0.1:" + port + "/login"
        TestAuthUtils.authorizeTestRestTemplate(testRestTemplate, loginUrl, username, password)
    }

    private String userEndpoint(String username) {
        potapaasUrl() + "/user/" + username
    }

    private String userEndpoint() {
        potapaasUrl() + "/user"
    }

    private String potapaasUrl() {
        "http://127.0.0.1:" + port
    }
}
