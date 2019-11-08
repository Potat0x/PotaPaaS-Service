package pl.potat0x.potapaas.potapaasservice.webhook;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.app.AppFacade;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDto;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDtoBuilder;
import pl.potat0x.potapaas.potapaasservice.app.AppResponseDto;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;
import pl.potat0x.potapaas.potapaasservice.validator.UuidValidator;

import static io.vavr.API.*;
import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@Service
class WebhookFacade {

    @Autowired
    private AppFacade appFacade;

    @Autowired
    WebhookFacade(AppFacade appFacade) {
        this.appFacade = appFacade;
    }

    Either<ErrorMessage, AppResponseDto> handleWebhook(String appUuid, String eventSourceBranch, HmacVerifier hmacVerifier) {

        if (!UuidValidator.checkIfValid(appUuid)) {
            return Either.left(message(appUuid + " is not a valid UUID", 400));
        }

        if (eventSourceBranch == null || eventSourceBranch.isEmpty()) {
            return Either.left(message("Event source branch is not specified", 400));
        }

        Either<ErrorMessage, AppResponseDto> appDetailsEither = appFacade.getAppDetails(appUuid);
        if (appDetailsEither.isLeft()) {
            return appDetailsEither;
        }

        AppResponseDto appResponseDto = appDetailsEither.get();
        try {
            if (!hmacVerifier.isMessageAuthentic(appResponseDto.getWebhookSecret())) {
                return Either.left(message("Invalid secret", 401));
            }
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(message("Error while request verification", 500));
        }

        Option<String> appCannotBeAutoredeployed = Match(appResponseDto).option(
                Case($(app -> !app.isAutodeployEnabled()), "Autodeploy is disabled"),
                Case($(app -> !app.getSourceBranchName().equals(eventSourceBranch)), "Event source branch is different than app source branch")
        );

        if (appCannotBeAutoredeployed.isDefined()) {
            String message = appCannotBeAutoredeployed.get();
            System.out.println("Webhook: " + message);
            return Either.left(message(message, 409));
        }

        AppRequestDto requestDtoForRedeployment = appResponseDtoToRequestDto(appResponseDto)
                .withCommitHash(null)
                .build();
        return appFacade.redeployApp(appUuid, requestDtoForRedeployment);
    }

    private AppRequestDtoBuilder appResponseDtoToRequestDto(AppResponseDto responseDto) {
        return new AppRequestDtoBuilder()
                .withName(responseDto.getName())
                .withType(responseDto.getType().name())
                .withSourceRepoUrl(responseDto.getSourceRepoUrl())
                .withSourceBranchName(responseDto.getSourceBranchName())
                .withAutodeployEnabled(responseDto.isAutodeployEnabled())
                .withCommitHash(responseDto.getCommitHash())
                .withDatastoreUuid(responseDto.getDatastoreUuid());
    }
}
