package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.core.AppManager;
import pl.potat0x.potapaas.potapaasservice.core.AppManagerFactory;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@Service
public class AppFacade {

    private static final Set<String> redeployLocks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Lock redeployLocksSetLock = new ReentrantLock();

    private final AppRepository appRepository;
    private final AppManagerFactory appManagerFactory;
    private final AppInstanceRepository appInstanceRepository;

    @Autowired
    AppFacade(AppRepository appRepository, AppManagerFactory appManagerFactory, AppInstanceRepository appInstanceRepository) {
        this.appRepository = appRepository;
        this.appInstanceRepository = appInstanceRepository;
        this.appManagerFactory = appManagerFactory;
    }

    public Either<ErrorMessage, AppResponseDto> createAndDeployApp(AppRequestDto requestDto) {
        AppManager appManager = buildAppManagerForNewApp(requestDto);
        return appManager.deploy().map(appUuid -> {
            AppEntity appEntity = buildAppEntity(appManager, requestDto).build();
            appRepository.save(appEntity);
            return buildResponseDto(appManager, appEntity);
        });
    }

    Either<ErrorMessage, AppResponseDto> redeployApp(String appUuid, AppRequestDto requestDto) {
        boolean redeployNotRunning;
        redeployLocksSetLock.lock();
        try {
            redeployNotRunning = redeployLocks.add(appUuid);
        } finally {
            redeployLocksSetLock.unlock();
        }

        if (redeployNotRunning) {
            try {
                return redeploy(appUuid, requestDto);
            } finally {
                redeployLocks.remove(appUuid);
            }
        } else {
            return Either.left(message("Redeploy already started", 429));
        }
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

    Either<ErrorMessage, String> deleteApp(String appUuid) {
        return getAppEntity(appUuid)
                .flatMap(appEntity -> {
                    AppManager appManager = buildAppManagerForExistingApp(appEntity);
                    return appManager
                            .killApp()
                            .flatMap(killedContainerId -> appManager.removeApp())
                            .peek(deletedContainerId -> appRepository.delete(appEntity))
                            .map(deletedContainerId -> appUuid);
                });
    }

    private Either<ErrorMessage, AppResponseDto> redeploy(String appUuid, AppRequestDto requestDto) {
        return getAppManagerForRedeploying(appUuid, requestDto).flatMap(appManager -> getAppEntityForRedeploying(appUuid, requestDto).flatMap(appEntity -> {
                    Long oldAppInstanceId = appEntity.getAppInstance().getId();
                    return appManager.redeploy().map(oldContainerId -> {
                        appEntity.setAppInstance(new AppInstanceEntity(appManager.getContainerId(), appManager.getImageId()));
                        appRepository.save(appEntity);
                        appInstanceRepository.deleteById(oldAppInstanceId);
                        return buildResponseDto(appManager, appEntity);
                    });
                })
        );
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

    private AppEntityBuilder buildAppEntity(AppManager appManager, AppRequestDto requestDto) {
        return new AppEntityBuilder()
                .withAppInstance(new AppInstanceEntity(appManager.getContainerId(), appManager.getImageId()))
                .withType(AppType.valueOf(requestDto.getType()))
                .withUuid(appManager.getAppUuid())
                .withName(requestDto.getName())
                .withSourceRepoUrl(requestDto.getSourceRepoUrl())
                .withSourceBranchName(requestDto.getSourceBranchName())
                .withCommitHash(requestDto.getCommitHash())
                .withDatastoreUuid(requestDto.getDatastoreUuid());
    }

    private Either<ErrorMessage, AppEntity> getAppEntityForRedeploying(String appUuid, AppRequestDto requestDto) {
        return getAppEntity(appUuid).map(appEntity -> {
            appEntity.setName(requestDto.getName());
            appEntity.setType(AppType.valueOf(requestDto.getType()));
            appEntity.setSourceRepoUrl(requestDto.getSourceRepoUrl());
            appEntity.setSourceBranchName(requestDto.getSourceBranchName());
            appEntity.setCommitHash(requestDto.getCommitHash());
            appEntity.setDatastoreUuid(requestDto.getDatastoreUuid());
            return appEntity;
        });
    }

    private Either<ErrorMessage, AppManager> getAppManagerForRedeploying(String appUuid, AppRequestDto appRequestDto) {
        return getAppEntityForRedeploying(appUuid, appRequestDto)
                .map(appEntity -> buildAppManagerForRedeployingApp(appEntity, appRequestDto));
    }

    private AppManager buildAppManagerForRedeployingApp(AppEntity app, AppRequestDto requestDto) {
        AppInstanceEntity instance = app.getAppInstance();
        return appManagerFactory.forExistingApp(requestDto, app.getType(), app.getUuid(), instance.getContainerId());
    }

    private AppManager buildAppManagerForExistingApp(AppEntity app) {
        AppInstanceEntity instance = app.getAppInstance();
        return appManagerFactory.forExistingApp(null, app.getType(), app.getUuid(), instance.getContainerId());
    }

    private AppManager buildAppManagerForNewApp(AppRequestDto appRequestDto) {
        return appManagerFactory.forNewApp(appRequestDto);
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
                .withCommitHash(appEntity.getCommitHash())
                .withDatastoreUuid(appEntity.getDatastoreUuid())
                .build();
    }
}
