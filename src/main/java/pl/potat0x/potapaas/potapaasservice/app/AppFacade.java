package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.core.AppManager;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@Service
class AppFacade {

    private final AppRepository appRepository;

    @Autowired
    AppFacade(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    Either<ErrorMessage, AppResponseDto> createAndDeployApp(AppRequestDto requestDto) {
        AppManager appManager = buildAppManagerForNewApp(requestDto);
        return appManager.deploy().map(x -> {
            AppEntity appEntity = buildAppEntity(appManager);
            appRepository.save(appEntity);
            return buildResponseDto(appManager, appEntity);
        });
    }
    
    private AppEntity buildAppEntity(AppManager appManager) {
        return new AppEntityBuilder()
                .withAppInstance(new AppInstanceEntity(appManager.getContainerId(), appManager.getImageId()))
                .withType(appManager.getAppType())
                .withUuid(appManager.getPotapaasAppId())
                .withName(appManager.getAppName())
                .withSourceRepoUrl(appManager.getGitRepoUrl())
                .withSourceBranchName(appManager.getBranchName())
                .build();
    }

    private AppManager buildAppManagerForExistingApp(AppEntity app) {
        AppInstanceEntity instance = app.getAppInstance();
        return AppManager.forExistingApp(
                app.getUuid(),
                app.getName(),
                app.getType(),
                app.getSourceRepoUrl(),
                app.getSourceBranchName(),
                instance.getContainerId(),
                instance.getImageId()
        );
    }

    private AppManager buildAppManagerForNewApp(AppRequestDto dto) {
        AppType appType = AppType.valueOf(dto.getType());
        return AppManager.createApp(dto.getName(), appType, dto.getSourceRepoUrl(), dto.getSourceBranchName());
    }

    private AppResponseDto buildResponseDto(AppManager app, AppEntity appEntity) {
        return new AppResponseDtoBuilder()
                .withAppId(appEntity.getUuid())
                .withName(appEntity.getName())
                .withType(appEntity.getType().userFriendlyName)
                .withSourceRepoUrl(appEntity.getSourceRepoUrl())
                .withSourceBranchName(appEntity.getSourceBranchName())
                .withCreatedAt(appEntity.getCreatedAt())
                .withStatus(app.getStatus().getOrElse("not deployed"))
                .withExposedPort(app.getPort().map(Integer::parseInt).getOrElse(-1))
                .build();
    }
}
