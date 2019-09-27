package pl.potat0x.potapaas.potapaasservice.datastore;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.potat0x.potapaas.potapaasservice.app.AppEntity;
import pl.potat0x.potapaas.potapaasservice.app.AppRepository;
import pl.potat0x.potapaas.potapaasservice.core.DatastoreManager;
import pl.potat0x.potapaas.potapaasservice.core.DockerContainerManager;
import pl.potat0x.potapaas.potapaasservice.core.DockerNetworkManager;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

@Service
class DatastoreFacade {

    private final DatastoreRepository datastoreRepository;
    private final DockerContainerManager containerManager;
    private final AppRepository appRepository;

    @Autowired
    public DatastoreFacade(DatastoreRepository datastoreRepository, AppRepository appRepository) {
        this.datastoreRepository = datastoreRepository;
        this.appRepository = appRepository;
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

        List<String> attachedApps = appRepository.findAllByDatastoreUuid(datastoreUuid)
                .stream()
                .map(AppEntity::getUuid)
                .collect(Collectors.toList());
        return Either.right(createResponseDto(datastoreEntity, attachedApps));
    }

    private DatastoreResponseDto createResponseDto(DatastoreEntity datastoreEntity) {
        return createResponseDto(datastoreEntity, Collections.emptyList());
    }

    private DatastoreResponseDto createResponseDto(DatastoreEntity datastoreEntity, List<String> attachedApps) {
        return new DatastoreResponseDto(datastoreEntity.getUuid(), datastoreEntity.getName(), datastoreEntity.getType().toString(), attachedApps);
    }

    private String randomUuid() {
        return UUID.randomUUID().toString();
    }
}
