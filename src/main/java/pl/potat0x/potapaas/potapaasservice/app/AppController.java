package pl.potat0x.potapaas.potapaasservice.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.potat0x.potapaas.potapaasservice.api.ResponseResolver;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

@RestController
@RequestMapping("/app")
class AppController {

    private final AppFacade facade;

    @Autowired
    AppController(AppFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    ResponseEntity createApp(@RequestBody AppRequestDto requestDto) {

        Validation<Seq<String>, AppRequestDto> validate = new AppRequestDtoValidator().validate(requestDto);

        if (validate.isValid()) {
            Either<ErrorMessage, AppResponseDto> deploymentResult = facade.createAndDeployApp(requestDto);
            return ResponseResolver.toResponseEntity(deploymentResult, HttpStatus.CREATED);
        } else {
            StringBuilder message = new StringBuilder();
            message.append(validate.getError().toStream().reduce((a, b) -> a + "\n" + b));
            try {
                ObjectWriter prettyJsonMapper = new ObjectMapper().writerWithDefaultPrettyPrinter();
                message.append("\n\nExample of valid request:\n")
                        .append(prettyJsonMapper.writeValueAsString(validAppRequestExample()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return ResponseResolver.toErrorResponseEntity(message.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private AppRequestDto validAppRequestExample() {
        return new AppRequestDtoBuilder()
                .withName("hello-world-app")
                .withType("NODEJS")
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok")
                .build();
    }
}