package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.utils.NameValidator;
import pl.potat0x.potapaas.potapaasservice.utils.EnumValidator;

import java.net.MalformedURLException;
import java.net.URL;

final class AppRequestDtoValidator {

    Validation<Seq<String>, AppRequestDto> validate(AppRequestDto requestDto) {
        return Validation.combine(NameValidator.validate(requestDto.getName(), "app name"),
                EnumValidator.checkIfEnumContainsConstant(requestDto.getType(), AppType.class, "app type"),
                validateUrl(requestDto.getSourceRepoUrl()),
                validateBranchName(requestDto.getSourceBranchName()),
                NameValidator.validate(requestDto.getDatastoreName(), "datastore name", true)
        ).ap(AppRequestDto::new);
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
