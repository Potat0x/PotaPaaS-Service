package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.validator.CommitHashValidator;
import pl.potat0x.potapaas.potapaasservice.validator.EnumValidator;
import pl.potat0x.potapaas.potapaasservice.validator.NameValidator;
import pl.potat0x.potapaas.potapaasservice.validator.UuidValidator;

import java.net.MalformedURLException;
import java.net.URL;

final class AppRequestDtoValidator {

    Validation<Seq<String>, AppRequestDto> validate(AppRequestDto requestDto) {
        return Validation.combine(NameValidator.validate(requestDto.getName(), "app name"),
                EnumValidator.checkIfEnumContainsConstant(requestDto.getType(), AppType.class, "app type"),
                validateUrl(requestDto.getSourceRepoUrl()),
                validateBranchName(requestDto.getSourceBranchName()),
                CommitHashValidator.validate(requestDto.getCommitHash(), true),
                validateUuid(requestDto.getDatastoreUuid(), true, "datastore UUID")
        ).ap(AppRequestDto::new);
    }

    private Validation<String, String> validateUuid(String datastoreUuid, boolean nullable, String propertyName) {
        if ((nullable && datastoreUuid == null) || UuidValidator.checkIfValid(datastoreUuid)) {
            return Validation.valid(datastoreUuid);
        }
        return Validation.invalid("Invalid " + propertyName);
    }

    private Validation<String, String> validateUrl(String sourceRepoUrl) {
        if (sourceRepoUrl == null) {
            return Validation.invalid("sourceRepoUrl is mandatory");
        }
        try {
            new URL(sourceRepoUrl);
            return Validation.valid(sourceRepoUrl);
        } catch (MalformedURLException e) {
            return Validation.invalid("Invalid repository URL");
        }
    }

    private Validation<String, String> validateBranchName(String sourceBranchName) {
        if (sourceBranchName == null) {
            return Validation.invalid("sourceBranchName is mandatory");
        }
        return !sourceBranchName.isEmpty() ? Validation.valid(sourceBranchName) : Validation.invalid("Branch name must be not empty");
    }
}
