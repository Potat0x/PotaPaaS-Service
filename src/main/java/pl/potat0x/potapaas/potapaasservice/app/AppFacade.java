package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.core.AppManager;
import pl.potat0x.potapaas.potapaasservice.core.AppManagerFactory;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@Service
class AppFacade {

    private final AppRepository appRepository;
    private final AppManagerFactory appManagerFactory;

    @Autowired
    AppFacade(AppRepository appRepository, AppManagerFactory appManagerFactory) {
        this.appRepository = appRepository;
        this.appManagerFactory = appManagerFactory;
    }

    Either<ErrorMessage, AppResponseDto> createAndDeployApp(AppRequestDto requestDto) {
        AppManager appManager = buildAppManagerForNewApp(requestDto);
        return appManager.deploy().map(x -> {
            AppEntity appEntity = buildAppEntity(appManager);
            appRepository.save(appEntity);
            return buildResponseDto(appManager, appEntity);
        });
    }

    Either<ErrorMessage, AppResponseDto> getAppDetails(String appUuid) {
        AppEntity appEntity = appRepository.findOneByUuid(appUuid);

        if (appEntity != null) {
            AppManager appManager = buildAppManagerForExistingApp(appEntity);
            return Either.right(buildResponseDto(appManager, appEntity));
        } else {
            return Either.left(message("App not found", 404));
        }
    }

    Either<ErrorMessage, Object> deleteApp(String appUuid) {
        AppEntity appEntity = appRepository.findOneByUuid(appUuid);

        if (appEntity != null) {
            AppManager appManager = buildAppManagerForExistingApp(appEntity);
            appManager.killApp();
            appManager.removeApp();

            appRepository.delete(appEntity);
            return Either.right(null);
        } else {
            return Either.left(message("App not found", 404));
        }
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

        return appManagerFactory.forExistingApp(
                app.getType(),
                app.getUuid(),
                app.getName(),
                app.getSourceRepoUrl(),
                app.getSourceBranchName(),
                instance.getContainerId(),
                instance.getImageId()
        );
    }

    private AppManager buildAppManagerForNewApp(AppRequestDto dto) {
        AppType appType = AppType.valueOf(dto.getType());
        return appManagerFactory.createApp(appType, dto.getName(), dto.getSourceRepoUrl(), dto.getSourceBranchName());
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
