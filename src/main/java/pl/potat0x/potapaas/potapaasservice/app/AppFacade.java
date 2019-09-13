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

    Either<ErrorMessage, AppResponseDto> redeployApp(String appUuid) {
        return getAppManager(appUuid)
                .peek(AppManager::redeployApp)
                .flatMap(appManager -> Either.right(null));
    }

    Either<ErrorMessage, String> getAppLogs(String appUuid) {
        return getAppManager(appUuid)
                .flatMap(AppManager::getLogs);
    }

    Either<ErrorMessage, AppResponseDto> getAppDetails(String appUuid) {
        return getAppEntity(appUuid).map(appEntity -> {
            AppManager appManager = buildAppManagerForExistingApp(appEntity);
            return buildResponseDto(appManager, appEntity);
        });
    }

    Either<ErrorMessage, Object> deleteApp(String appUuid) {
        return getAppEntity(appUuid)
                .peek(appEntity -> {
                    AppManager appManager = buildAppManagerForExistingApp(appEntity);
                    appManager.killApp();
                    appManager.removeApp();

                    appRepository.delete(appEntity);
                })
                .flatMap(x -> Either.right(null));
    }

    private Either<ErrorMessage, AppEntity> getAppEntity(String appUuid) {
        AppEntity appEntity = appRepository.findOneByUuid(appUuid);
        if (appEntity != null) {
            return Either.right(appEntity);
        }
        return Either.left(message("App not found", 404));
    }

    private Either<ErrorMessage, AppManager> getAppManager(String appUuid) {
        return getAppEntity(appUuid)
                .map(this::buildAppManagerForExistingApp);
    }

    private AppEntity buildAppEntity(AppManager appManager) {
        return new AppEntityBuilder()
                .withAppInstance(new AppInstanceEntity(appManager.getContainerId(), appManager.getImageId()))
                .withType(appManager.getAppType())
                .withUuid(appManager.getAppUuid())
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
                .withAppUuid(appEntity.getUuid())
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
