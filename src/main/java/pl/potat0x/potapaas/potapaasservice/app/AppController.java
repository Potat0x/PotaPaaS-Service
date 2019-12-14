package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.collection.Seq;
import io.vavr.control.Either;
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
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.validator.UuidValidator;

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

        Validation<Seq<String>, AppRequestDto> validation = new AppRequestDtoValidator().validate(requestDto);

        if (validation.isValid()) {
            Either<ErrorMessage, AppResponseDto> deploymentResult = facade.createAndDeployApp(requestDto);
            return ResponseResolver.toResponseEntity(deploymentResult, HttpStatus.CREATED);
        }
        return ResponseResolver.toErrorResponseEntity(validation, HttpStatus.UNPROCESSABLE_ENTITY, validAppRequestExample());
    }

    @GetMapping("/{appUuid}")
    ResponseEntity getAppDetails(@PathVariable String appUuid) {
        if (UuidValidator.checkIfValid(appUuid)) {
            return ResponseResolver.toResponseEntity(facade.getAppDetails(appUuid), HttpStatus.OK);
        }
        return invalidUuidResponseEntity(appUuid);
    }

    @GetMapping
    ResponseEntity getUuidsAndNamesOfAllApps() {
        return ResponseResolver.toResponseEntity(facade.getUuidsAndNamesOfAllApps(), HttpStatus.OK);
    }

    @GetMapping("/{appUuid}/logs")
    ResponseEntity getAppLogs(@PathVariable String appUuid) {
        if (UuidValidator.checkIfValid(appUuid)) {
            return ResponseResolver.toResponseEntity(facade.getAppLogs(appUuid), HttpStatus.OK);
        }
        return invalidUuidResponseEntity(appUuid);
    }

    @DeleteMapping("/{appUuid}")
    ResponseEntity deleteApp(@PathVariable String appUuid) {
        if (UuidValidator.checkIfValid(appUuid)) {
            return ResponseResolver.toResponseEntity(facade.deleteApp(appUuid), HttpStatus.NO_CONTENT);
        }
        return invalidUuidResponseEntity(appUuid);
    }

    @PostMapping("/{appUuid}/redeploy")
    ResponseEntity redeployApp(@PathVariable String appUuid, @RequestBody AppRequestDto requestDto) {
        if (!UuidValidator.checkIfValid(appUuid)) {
            return invalidUuidResponseEntity(appUuid);
        }

        Validation<Seq<String>, AppRequestDto> validation = new AppRequestDtoValidator().validate(requestDto);
        if (!validation.isValid()) {
            return ResponseResolver.toErrorResponseEntity(validation, HttpStatus.UNPROCESSABLE_ENTITY, validAppRequestExample());
        }

        return ResponseResolver.toResponseEntity(facade.redeployApp(appUuid, requestDto), HttpStatus.OK);
    }

    @PostMapping("/{appUuid}/change-webhook-secret")
    ResponseEntity changeWebhookSecret(@PathVariable String appUuid, @RequestBody WebhookSecretRequestDto newSecret) {
        if (!UuidValidator.checkIfValid(appUuid)) {
            return invalidUuidResponseEntity(appUuid);
        }
        return ResponseResolver.toResponseEntity(facade.changeWebhookSecret(appUuid, newSecret), HttpStatus.OK);
    }

    private ResponseEntity invalidUuidResponseEntity(String invalidUuid) {
        return ResponseResolver.toErrorResponseEntity(invalidUuid + " is not a valid UUID", HttpStatus.BAD_REQUEST);
    }

    private AppRequestDto validAppRequestExample() {
        return new AppRequestDtoBuilder()
                .withName("hello-world-app")
                .withType(AppType.NODEJS.toString())
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok")
                .build();
    }
}
