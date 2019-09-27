package pl.potat0x.potapaas.potapaasservice.datastore;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.core.DatastoreManager;
import pl.potat0x.potapaas.potapaasservice.core.DockerContainerManager;
import pl.potat0x.potapaas.potapaasservice.core.DockerNetworkManager;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@Service
class DatastoreFacade {

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

        DatastoreManager datastoreManager = new DatastoreManager(containerManager, type, new DockerNetworkManager(PotapaasConfig.get("docker_api_uri")));
        return datastoreManager.createAndStartDatastore(datastoreUuid)
                .map(containerId -> new DatastoreEntity(datastoreUuid, type, name, randomUuid(), randomUuid(), containerId))
                .peek(datastoreRepository::save)
                .map(this::createResponseDto);
    }

    Either<ErrorMessage, DatastoreResponseDto> getDatastoreDetails(String datastoreUuid) {
        DatastoreEntity datastoreEntity = datastoreRepository.findOneByUuid(datastoreUuid);
        if (datastoreEntity == null) {
            return Either.left(message("Datastore not found", 404));
        }

        Set<String> attachedApps = datastoreRepository.findAllAppsConnectedToDatastore(datastoreUuid);
        return Either.right(createResponseDto(datastoreEntity, attachedApps));
    }

    private DatastoreResponseDto createResponseDto(DatastoreEntity datastoreEntity) {
        return createResponseDto(datastoreEntity, Collections.emptySet());
    }

    private DatastoreResponseDto createResponseDto(DatastoreEntity datastoreEntity, Set<String> attachedApps) {
        return new DatastoreResponseDto(datastoreEntity.getUuid(), datastoreEntity.getName(), datastoreEntity.getType().toString(), attachedApps);
    }

    private String randomUuid() {
        return UUID.randomUUID().toString();
    }
}
