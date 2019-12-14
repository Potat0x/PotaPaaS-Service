package pl.potat0x.potapaas.potapaasservice.app;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.api.UuidAndNameResponseDto;
import pl.potat0x.potapaas.potapaasservice.core.AppManager;
import pl.potat0x.potapaas.potapaasservice.core.AppManagerFactory;
import pl.potat0x.potapaas.potapaasservice.core.AppType;
import pl.potat0x.potapaas.potapaasservice.datastore.DatastoreFacade;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static pl.potat0x.potapaas.potapaasservice.security.AuthenticatedPrincipalInfo.getUserId;
import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@Service
public class AppFacade {

    private static final Set<String> redeployLocks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Lock redeployLocksSetLock = new ReentrantLock();

    private final AppRepository appRepository;
    private final AppManagerFactory appManagerFactory;
    private final AppInstanceRepository appInstanceRepository;
    private final DatastoreFacade datastoreFacade;

    @Autowired
    AppFacade(AppRepository appRepository, AppManagerFactory appManagerFactory, AppInstanceRepository appInstanceRepository, DatastoreFacade datastoreFacade) {
        this.appRepository = appRepository;
        this.appInstanceRepository = appInstanceRepository;
        this.appManagerFactory = appManagerFactory;
        this.datastoreFacade = datastoreFacade;
    }

    public Either<ErrorMessage, AppResponseDto> createAndDeployApp(AppRequestDto requestDto) {

        if (!isAppNameAvailable(requestDto.getName())) {
            return Either.left(appNameNotAvailableMessage(requestDto.getName()));
        }

        if (!checkIfDatastoreIsAvailableIfSpecifiedInRequest(requestDto)) {
            return Either.left(message("Datastore not found", 404));
        }

        AppManager appManager = buildAppManagerForNewApp(requestDto);
        return appManager.deploy().map(appUuid -> {
            AppEntity appEntity = buildAppEntity(getUserId(), appManager, requestDto).build();
            appRepository.save(appEntity);
            return buildResponseDto(appManager, appEntity);
        });
    }

    public Either<ErrorMessage, AppResponseDto> redeployApp(String appUuid, AppRequestDto requestDto) {
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

    public Either<ErrorMessage, AppResponseDto> getAppDetails(String appUuid) {
        return getAppEntity(appUuid).map(appEntity -> {
            AppManager appManager = buildAppManagerForExistingApp(appEntity);
            return buildResponseDto(appManager, appEntity);
        });
    }

    public Either<ErrorMessage, List<UuidAndNameResponseDto>> getUuidsAndNamesOfAllApps() {
        return Either.right(appRepository.findAll().stream()
                .map(appEntity -> new UuidAndNameResponseDto(appEntity.getUuid(), appEntity.getName()))
                .collect(Collectors.toList())
        );
    }

    public Either<ErrorMessage, String> deleteApp(String appUuid) {
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

    Either<ErrorMessage, AppResponseDto> changeWebhookSecret(String appUuid, WebhookSecretRequestDto secretRequestDto) {
        if (isNotNullAndEmpty(secretRequestDto.getSecret())) {
            return Either.left(message("Secret cannot be empty", 400));
        } else {
            return getAppEntity(appUuid)
                    .flatMap(appEntity -> {
                        String newSecret = getSecretFromRequestIfNotNullElseCreateRandom(secretRequestDto);
                        appEntity.setWebhookSecret(newSecret);
                        appRepository.save(appEntity);
                        return getAppDetails(appUuid);
                    });
        }
    }

    private Either<ErrorMessage, AppResponseDto> redeploy(String appUuid, AppRequestDto requestDto) {
        if (!checkIfAppCanBeRedeployedWithGivenName(appUuid, requestDto.getName())) {
            return Either.left(appNameNotAvailableMessage(requestDto.getName()));
        }

        if (!checkIfDatastoreIsAvailableIfSpecifiedInRequest(requestDto)) {
            return Either.left(message("Datastore not found", 404));
        }

        return getAppManagerForRedeploying(appUuid, requestDto).flatMap(appManager -> getAppEntityForRedeploying(appUuid, requestDto).flatMap(appEntity -> {
                    Long oldAppInstanceId = appEntity.getAppInstance().getId();
                    return appManager.redeploy().map(oldContainerId -> {
                        appEntity.setAppInstance(new AppInstanceEntity(appManager.getContainerId(), appManager.getImageId()));
                        appEntity.setCommitHash(appManager.getCommitHash());
                        appRepository.save(appEntity);
                        appInstanceRepository.deleteById(oldAppInstanceId);
                        return buildResponseDto(appManager, appEntity);
                    });
                })
        );
    }

    private boolean checkIfDatastoreIsAvailableIfSpecifiedInRequest(AppRequestDto requestDto) {
        String datastoreUuid = requestDto.getDatastoreUuid();
        if (datastoreUuid == null) {
            return true;
        }
        return checkIfDatastoreExists(datastoreUuid);
    }

    private Either<ErrorMessage, AppEntity> getAppEntity(String appUuid) {
        List<AppEntity> appEntity = appRepository.findOneByUuid(appUuid);
        if (!appEntity.isEmpty()) {
            return Either.right(appEntity.get(0));
        }
        return Either.left(message("App not found", 404));
    }

    private boolean checkIfDatastoreExists(String datastoreUuid) {
        return datastoreFacade.getDatastoreEntityByUuid(datastoreUuid).isRight();
    }

    private Either<ErrorMessage, AppManager> getAppManager(String appUuid) {
        return getAppEntity(appUuid)
                .map(this::buildAppManagerForExistingApp);
    }

    private AppEntityBuilder buildAppEntity(Long userId, AppManager appManager, AppRequestDto requestDto) {
        return new AppEntityBuilder()
                .withUserId(userId)
                .withAppInstance(new AppInstanceEntity(appManager.getContainerId(), appManager.getImageId()))
                .withType(AppType.valueOf(requestDto.getType()))
                .withUuid(appManager.getAppUuid())
                .withName(requestDto.getName())
                .withSourceRepoUrl(requestDto.getSourceRepoUrl())
                .withSourceBranchName(requestDto.getSourceBranchName())
                .withAutodeployEnabled(requestDto.isAutodeployEnabled())
                .withWebhookSecret(generateRandomWebhookSecret())
                .withCommitHash(appManager.getCommitHash())
                .withDatastoreUuid(requestDto.getDatastoreUuid());
    }

    private String getSecretFromRequestIfNotNullElseCreateRandom(WebhookSecretRequestDto secretRequestDto) {
        return secretRequestDto.getSecret() != null ? secretRequestDto.getSecret() : generateRandomWebhookSecret();
    }

    private boolean isNotNullAndEmpty(String secret) {
        return secret != null && secret.isEmpty();
    }

    private String generateRandomWebhookSecret() {
        return "secret_" + UUID.randomUUID();
    }

    private Either<ErrorMessage, AppEntity> getAppEntityForRedeploying(String appUuid, AppRequestDto requestDto) {
        if (requestDto == null) {
            return getAppEntity(appUuid);
        }
        return getAppEntity(appUuid).map(appEntity -> {
            appEntity.setName(requestDto.getName());
            appEntity.setType(AppType.valueOf(requestDto.getType()));
            appEntity.setSourceRepoUrl(requestDto.getSourceRepoUrl());
            appEntity.setSourceBranchName(requestDto.getSourceBranchName());
            appEntity.setAutodeployEnabled(requestDto.isAutodeployEnabled());
            appEntity.setCommitHash(requestDto.getCommitHash());
            appEntity.setDatastoreUuid(requestDto.getDatastoreUuid());
            return appEntity;
        });
    }

    private boolean isAppNameAvailable(String name) {
        return appRepository.countByName(name) == 0;
    }

    private boolean checkIfAppIsOwnerOfGivenName(String appUuid, String name) {
        return appRepository.countByUuidAndName(appUuid, name) == 1;
    }

    private ErrorMessage appNameNotAvailableMessage(String name) {
        return message("App name \"" + name + "\" is not available", 409);
    }

    private boolean checkIfAppCanBeRedeployedWithGivenName(String appUuid, String name) {
        return isAppNameAvailable(name) || checkIfAppIsOwnerOfGivenName(appUuid, name);
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
                .withType(appEntity.getType())
                .withSourceRepoUrl(appEntity.getSourceRepoUrl())
                .withSourceBranchName(appEntity.getSourceBranchName())
                .withAutodeployEnabled(appEntity.isAutodeployEnabled())
                .withWebhookSecret(appEntity.getWebhookSecret())
                .withCreatedAt(appEntity.getCreatedAt())
                .withStatus(app.getStatus().getOrElse("not deployed"))
                .withExposedPort(app.getPort().map(Integer::parseInt).getOrElse(-1))
                .withCommitHash(appEntity.getCommitHash())
                .withDatastoreUuid(appEntity.getDatastoreUuid())
                .build();
    }
}
