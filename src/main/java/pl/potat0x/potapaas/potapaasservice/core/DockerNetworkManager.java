package pl.potat0x.potapaas.potapaasservice.core;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkCreation;
import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;

import java.util.List;
import java.util.stream.Collectors;

import static pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.CaseBuilderStart.exception;

final public class DockerNetworkManager {

    private final DockerClient docker;

    public DockerNetworkManager(String dockerClientUri) {
        docker = new DefaultDockerClient(dockerClientUri);
    }

    Either<ErrorMessage, String> createNetwork(String name) {
        NetworkConfig networkConfig = NetworkConfig
                .builder()
                .name(name)
                .attachable(true)
                .build();
        try {
            NetworkCreation networkCreation = docker.createNetwork(networkConfig);
            return Either.right(networkCreation.id());
        } catch (Exception e) {
            e.printStackTrace();
            return convertExceptionToErrorMessage(e);
        }
    }

    Either<ErrorMessage, String> connectContainerToNetwork(String containerId, String networkId) {
        try {
            docker.connectToNetwork(containerId, networkId);
            return Either.right(networkId);
        } catch (Exception e) {
            e.printStackTrace();
            return convertExceptionToErrorMessage(e);
        }
    }

    Either<ErrorMessage, String> disconnectContainerFromNetwork(String containerId, String networkId) {
        try {
            docker.disconnectFromNetwork(containerId, networkId);
            return Either.right(networkId);
        } catch (Exception e) {
            e.printStackTrace();
            return convertExceptionToErrorMessage(e);
        }
    }

    Either<ErrorMessage, List<String>> listContainersConnectedToNetwork(String networkUuid) {
        try {
            List<String> containerIds = docker.listContainers()
                    .stream()
                    .map(Container::id)
                    .filter(containerId ->
                            {
                                try {
                                    return docker.inspectContainer(containerId).networkSettings().networks().containsKey(networkUuid);
                                } catch (DockerException | InterruptedException e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            }
                    ).collect(Collectors.toList());
            return Either.right(containerIds);
        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
            return ExceptionMapper.map(e).of(
                    exception(InterruptedException.class, DockerException.class).to(CoreErrorMessage.SERVER_ERROR)
            );
        }
    }

    private Either<ErrorMessage, String> convertExceptionToErrorMessage(Exception e) {
        return ExceptionMapper.map(e).of(
                exception(NetworkNotFoundException.class).to(CoreErrorMessage.CONTAINER_NOT_FOUND),
                exception(DockerException.class, NotFoundException.class).to(CoreErrorMessage.SERVER_ERROR)
        );
    }
}
