package pl.potat0x.potapaas.potapaasservice.core;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkCreation;
import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;

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
            return convertExceptionToErrorMessage(e);
        }
    }

    Either<ErrorMessage, String> connectContainerToNetwork(String containerId, String networkId) {
        try {
            docker.connectToNetwork(containerId, networkId);
            return Either.right(networkId);
        } catch (Exception e) {
            return convertExceptionToErrorMessage(e);
        }
    }

    Either<ErrorMessage, String> disconnectContainerFromNetwork(String containerId, String networkId) {
        try {
            docker.disconnectFromNetwork(containerId, networkId);
            return Either.right(networkId);
        } catch (Exception e) {
            return convertExceptionToErrorMessage(e);
        }
    }

    private Either<ErrorMessage, String> convertExceptionToErrorMessage(Exception e) {
        return ExceptionMapper.map(e).of(
                exception(NetworkNotFoundException.class).to(CoreErrorMessage.CONTAINER_NOT_FOUND),
                exception(NotFoundException.class).to(CoreErrorMessage.SERVER_ERROR),
                exception(DockerException.class).to(CoreErrorMessage.SERVER_ERROR)
        );
    }
}
