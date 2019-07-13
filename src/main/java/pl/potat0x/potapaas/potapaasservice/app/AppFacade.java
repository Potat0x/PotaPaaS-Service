package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.core.AppDeployment;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.time.LocalDateTime;


@Service
class AppFacade {
    Either<ErrorMessage, AppResponseDto> createAndDeployApp(AppRequestDto requestDto) {
        AppDeployment appDeployment = deploymentFromRequestDto(requestDto);
        return appDeployment.deploy().map(this::getAppDetails);
    }

    private AppDeployment deploymentFromRequestDto(AppRequestDto requestDto) {
        AppType appType = AppType.valueOf(requestDto.getType());
        return new AppDeployment(appType, requestDto.getSourceRepoUrl(), requestDto.getSourceBranchName());
    }

    private AppResponseDto getAppDetails(String appId) { //todo: return values from database
        return new AppResponseDto(appId, "type", "repo_url", "branch_name",
                LocalDateTime.now(), "status", "123", 8088);
    }
}
