package pl.potat0x.potapaas.potapaasservice.user;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.potat0x.potapaas.potapaasservice.api.ResponseResolver;

@RestController
@RequestMapping("/user")
class UserController {

    private final UserFacade userFacade;

    @Autowired
    UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @GetMapping("/{username}")
    ResponseEntity getUser(@PathVariable String username) {
        return ResponseResolver.toResponseEntity(userFacade.getUserDetails(username), HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity createUser(@RequestBody UserRequestDto requestDto) {

        Validation<Seq<String>, UserRequestDto> validation = new UserRequestDtoValidator().validate(requestDto);
        if (!validation.isValid()) {
            return ResponseResolver.toErrorResponseEntity(validation, HttpStatus.UNPROCESSABLE_ENTITY, validUserRequestDtoExample());
        }

        return ResponseResolver.toResponseEntity(userFacade.createUser(requestDto), HttpStatus.CREATED);
    }

    @PostMapping("/{username}/password")
    ResponseEntity changeUserPassword(@PathVariable String username, @RequestBody ChangePasswordRequestDto requestDto) {

        Validation<Seq<String>, ChangePasswordRequestDto> validation = new ChangePasswordRequestDtoValidator().validate(requestDto);
        if (!validation.isValid()) {
            return ResponseResolver.toErrorResponseEntity(validation, HttpStatus.UNPROCESSABLE_ENTITY, validUserChangePasswordRequestDtoExample());
        }

        return ResponseResolver.toResponseEntity(userFacade.changePassword(username, requestDto), HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{username}")
    ResponseEntity deleteUser(@PathVariable String username) {
        return ResponseResolver.toResponseEntity(userFacade.deleteUser(username), HttpStatus.NO_CONTENT);
    }

    private UserRequestDto validUserRequestDtoExample() {
        return new UserRequestDto("user-name-123", "dA*&Kx31fvE*UYf7s", "user123@example.com");
    }

    private ChangePasswordRequestDto validUserChangePasswordRequestDtoExample() {
        return new ChangePasswordRequestDto("dA*&Kx31fvE*UYf7s", "&q2(aDy6_rVj3dZp8!RX");
    }
}
