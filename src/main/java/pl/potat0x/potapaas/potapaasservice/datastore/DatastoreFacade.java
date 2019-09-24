package pl.potat0x.potapaas.potapaasservice.datastore;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.core.DatastoreManager;
import pl.potat0x.potapaas.potapaasservice.core.DockerContainerManager;
import pl.potat0x.potapaas.potapaasservice.core.DockerNetworkManager;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.util.UUID;

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

    private DatastoreResponseDto createResponseDto(DatastoreEntity datastoreEntity) {
        return new DatastoreResponseDto(datastoreEntity.getUuid(), datastoreEntity.getName(), datastoreEntity.getType().toString());
    }

    private String randomUuid() {
        return UUID.randomUUID().toString();
    }
}
