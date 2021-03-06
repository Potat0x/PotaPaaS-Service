package pl.potat0x.potapaas.potapaasservice.datastore;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.api.UuidAndNameResponseDto;
import pl.potat0x.potapaas.potapaasservice.core.DockerContainerManager;
import pl.potat0x.potapaas.potapaasservice.core.DockerNetworkManager;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static io.vavr.Predicates.isIn;
import static pl.potat0x.potapaas.potapaasservice.security.AuthenticatedPrincipalInfo.getUserId;
import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@Service
public class DatastoreFacade {

    private final DatastoreRepository datastoreRepository;
    private final DockerContainerManager containerManager;

    @Autowired
    public DatastoreFacade(DatastoreRepository datastoreRepository) {
        this.datastoreRepository = datastoreRepository;
        containerManager = new DockerContainerManager(PotapaasConfig.get("docker_api_uri"));
    }

    Either<ErrorMessage, DatastoreResponseDto> createDatastore(DatastoreRequestDto requestDto) {
        String datastoreUuid = UUID.randomUUID().toString();
        String name = requestDto.getName();
        DatastoreType type = DatastoreType.valueOf(requestDto.getType());

        DatastoreManager datastoreManager = createDatastoreManager(type);
        return datastoreManager.createAndStartDatastore(datastoreUuid)
                .map(containerId -> new DatastoreEntity(datastoreUuid, getUserId(), type, name, randomUuid(), randomUuid(), containerId))
                .peek(datastoreRepository::save)
                .map(this::createResponseDto);
    }

    Either<ErrorMessage, DatastoreResponseDto> getDatastoreDetails(String datastoreUuid) {
        return getDatastoreEntityByUuid(datastoreUuid)
                .map(datastoreEntity -> {
                    Set<String> attachedApps = datastoreRepository.findAllAppsConnectedToDatastore(datastoreUuid);
                    return createResponseDto(datastoreEntity, attachedApps);
                });
    }

    Either<ErrorMessage, List<UuidAndNameResponseDto>> getUuidsAndNamesOfAllDatastores() {
        return Either.right(
                datastoreRepository.findAll().stream()
                        .map(datastoreEntity -> new UuidAndNameResponseDto(datastoreEntity.getUuid(), datastoreEntity.getName()))
                        .collect(Collectors.toList())
        );
    }

    Either<ErrorMessage, String> deleteDatastore(String datastoreUuid) {
        return getDatastoreEntityByUuid(datastoreUuid)
                .flatMap(datastoreEntity -> {
                    if (datastoreRepository.findAllAppsConnectedToDatastore(datastoreUuid).isEmpty()) {
                        DatastoreManager datastoreManager = createDatastoreManager(datastoreEntity.getType());
                        datastoreManager.stopDatastore(datastoreEntity.getContainerId())
                                .peek(containerId -> datastoreRepository.delete(datastoreEntity));
                        return Either.right(datastoreUuid);
                    }
                    return Either.left(message("Datastore cant be deleted: there are apps attached to it", 409));
                });
    }

    public Either<ErrorMessage, DatastoreEntity> getDatastoreEntityByUuid(String datastoreUuid) {
        List<DatastoreEntity> datastores = datastoreRepository.findAllByUuid(datastoreUuid);
        if (datastores.isEmpty()) {
            return Either.left(message("Datastore not found", 404));
        }
        return Either.right(datastores.get(0));
    }

    private DatastoreManager createDatastoreManager(DatastoreType type) {
        return new DatastoreManager(containerManager, type, new DockerNetworkManager(PotapaasConfig.get("docker_api_uri")), getDatastoreReadinessWaiter(type));
    }

    private DatastoreReadinessWaiter getDatastoreReadinessWaiter(DatastoreType datastoreType) {
        return Match(datastoreType).of(
                Case($(isIn(DatastoreType.POSTGRESQL, DatastoreType.MYSQL, DatastoreType.MARIADB)), (Supplier<DatastoreReadinessWaiter>) () -> new SqlDatastoreReadinessWaiter(datastoreType, PotapaasConfig.getInt("datastore_startup_timeout_in_millis")))
        );
    }

    private DatastoreResponseDto createResponseDto(DatastoreEntity datastoreEntity) {
        return createResponseDto(datastoreEntity, Collections.emptySet());
    }

    private DatastoreResponseDto createResponseDto(DatastoreEntity datastoreEntity, Set<String> attachedApps) {
        DatastoreManager datastoreManager = createDatastoreManager(datastoreEntity.getType());
        String status = datastoreManager.getStatus(datastoreEntity.getContainerId())
                .getOrElseGet(errorMessage -> "Unknown datastore status: " + errorMessage.getText() + ": " + errorMessage.getDetails());
        return new DatastoreResponseDto(datastoreEntity.getUuid(), datastoreEntity.getName(), datastoreEntity.getType(), datastoreEntity.getCreatedAt(), status, attachedApps);
    }

    private String randomUuid() {
        return UUID.randomUUID().toString();
    }
}
