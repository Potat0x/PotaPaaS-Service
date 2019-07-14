package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.core.AppDeployment;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.time.LocalDateTime;

@Service
class AppFacade {

    private final AppRepository appRepository;

    @Autowired
    AppFacade(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    Either<ErrorMessage, AppResponseDto> createAndDeployApp(AppRequestDto requestDto) {
        AppDeployment appDeployment = deploymentFromRequestDto(requestDto);
        return appDeployment.deploy().map(x -> {
            AppEntity appEntity = saveAppToDatabase(appDeployment);
            return responseDtoFromEntity(appDeployment, appEntity);
        });
    }

    private AppEntity saveAppToDatabase(AppDeployment appDeployment) {
        AppInstanceEntity instance = new AppInstanceEntity(appDeployment.getContainerId(), appDeployment.getImageId());
        AppEntity appEntity = new AppEntity(
                appDeployment.getPotapaasAppId(),
                instance, appDeployment.getAppName(),
                appDeployment.getAppType(),
                appDeployment.getGithubRepoUrl(),
                appDeployment.getBranchName()
        );
        return appRepository.save(appEntity);
    }

    private AppDeployment deploymentFromRequestDto(AppRequestDto requestDto) {
        AppType appType = AppType.valueOf(requestDto.getType());
        return new AppDeployment(requestDto.getName(), appType, requestDto.getSourceRepoUrl(), requestDto.getSourceBranchName());
    }

    private AppResponseDto responseDtoFromEntity(AppDeployment app, AppEntity appEntity) {
        return new AppResponseDtoBuilder()
                .withAppId(appEntity.getUuid())
                .withName(appEntity.getName())
                .withType(appEntity.getType().userFriendlyName)
                .withSourceRepoUrl(appEntity.getSourceRepoUrl())
                .withSourceBranchName(appEntity.getSourceBranchName())

                .withCreatedAt(app.getCreationDate().getOrElse(((LocalDateTime) null)))
                .withStatus(app.getStatus().getOrElse(""))
                .withExposedPort(app.getPort().map(Integer::parseInt).getOrElse(-1))
                .build();
    }
}
