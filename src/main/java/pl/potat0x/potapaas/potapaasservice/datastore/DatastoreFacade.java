package pl.potat0x.potapaas.potapaasservice.datastore;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.core.DatastoreManager;
import pl.potat0x.potapaas.potapaasservice.core.DockerContainerManager;
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

    Either<ErrorMessage, DatastoreDto> createDatastore(DatastoreDto datastoreRequestDto) {
        String name = datastoreRequestDto.getName();
        DatastoreType type = DatastoreType.valueOf(datastoreRequestDto.getType());

        DatastoreManager datastoreManager = new DatastoreManager(containerManager, type);
        return datastoreManager.createAndStartDatastore()
                .map(containerId -> new DatastoreEntity(type, name, randomUuid(), randomUuid(), containerId))
                .peek(datastoreRepository::save)
                .map(datastoreEntity -> datastoreRequestDto);
    }

    private String randomUuid() {
        return UUID.randomUUID().toString();
    }
}
