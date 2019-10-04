package pl.potat0x.potapaas.potapaasservice.datastore;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.core.DockerContainerManager;
import pl.potat0x.potapaas.potapaasservice.core.DockerNetworkManager;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

public final class DatastoreManager {
    private final DockerContainerManager containerManager;
    private final DatastoreType datastoreType;
    private final DockerNetworkManager networkManager;
    private final DatastoreReadinessWaiter datastoreReadinessWaiter;
    private String containerId;

    public DatastoreManager(DockerContainerManager containerManager, DatastoreType datastoreType, DockerNetworkManager networkManager, DatastoreReadinessWaiter datastoreReadinessWaiter) {
        this.containerManager = containerManager;
        this.datastoreType = datastoreType;
        this.networkManager = networkManager;
        this.datastoreReadinessWaiter = datastoreReadinessWaiter;
    }

    public Either<ErrorMessage, String> createAndStartDatastore(String datastoreUuid) {
        HostConfig hostConfig = HostConfig.builder()
                .publishAllPorts(true)
                .build();

        ContainerConfig.Builder config = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(datastoreType.dockerRepository)
                .exposedPorts(PotapaasConfig.get("default_datastore_port"))
                .env("POSTGRES_PASSWORD=docker");

        return containerManager.runContainer(config, datastoreUuid)
                .peek(containerId -> this.containerId = containerId)
                .flatMap(this::waitForDatastore)
                .flatMap(datastoreWaitMessage -> prepareDatastoreNetwork(datastoreUuid))
                .peek(networkId -> connectDatastoreToNetwork(this.containerId, networkId))
                .map(networkId -> containerId);
    }

    public Either<ErrorMessage, String> getPort() {
        return containerManager.getHostPort(containerId, PotapaasConfig.get("default_datastore_port"));
    }

    public Either<ErrorMessage, String> getStatus(String containerId) {
        return containerManager.getStatus(containerId);
    }

    private Either<ErrorMessage, String> prepareDatastoreNetwork(String networkName) {
        return networkManager.createNetwork(networkName);
    }

    private Either<ErrorMessage, String> connectDatastoreToNetwork(String containerId, String networkId) {
        return networkManager.connectContainerToNetwork(containerId, networkId);
    }

    private Either<ErrorMessage, String> waitForDatastore(String containerId) {
        return containerManager.getHostPort(containerId, PotapaasConfig.get("default_datastore_port"))
                .map(Integer::parseInt)
                .flatMap(datastorePort -> datastoreReadinessWaiter.waitUntilDatastoreIsAvailable("127.0.0.1", datastorePort, "postgres", "docker"));
    }
}
