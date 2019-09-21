package pl.potat0x.potapaas.potapaasservice.core;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.datastore.DatastoreType;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

public final class DatastoreManager {
    private final DockerContainerManager containerManager;
    private final DatastoreType datastoreType;
    private String containerId;

    public DatastoreManager(DockerContainerManager containerManager, DatastoreType datastoreType) {
        this.containerManager = containerManager;
        this.datastoreType = datastoreType;
    }

    public Either<ErrorMessage, String> createAndStartDatastore() {
        HostConfig hostConfig = HostConfig.builder()
                .publishAllPorts(true)
                .build();

        ContainerConfig.Builder config = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(datastoreType.dockerRepository)
                .exposedPorts(PotapaasConfig.get("default_datastore_port"))
                .env("POSTGRES_PASSWORD=docker");

        return containerManager.runContainer(config)
                .peek(containerId -> this.containerId = containerId);
    }

    public Either<ErrorMessage, String> getPort() {
        return containerManager.getHostPort(containerId);
    }
}
