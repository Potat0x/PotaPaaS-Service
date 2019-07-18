package pl.potat0x.potapaas.potapaasservice.core;

import io.vavr.control.Either;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;
import static pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.CaseBuilderStart.exception;

public class GitCloner {

    Either<ErrorMessage, String> cloneBranch(String repositoryUri, String branchName, String targetPath) {
        String clonedRepoDirectory = preparePathForClonedRepository(repositoryUri, targetPath);
        try (Git gitRepository = Git.cloneRepository()
                .setURI(repositoryUri)
                .setBranch(branchName)
                .setDirectory(new File(clonedRepoDirectory))
                .call()
        ) {
            if (gitRepository.branchList().call().size() > 0) {
                return Either.right(clonedRepoDirectory);
            }

            return Either.left(message("branch \"" + branchName + "\" not found in repository: " + repositoryUri, 404));
        } catch (Exception e) {
            return ExceptionMapper.map(e).of(
                    exception(GitAPIException.class, InvalidRemoteException.class).to(
                            message("Error while cloning \"" + branchName + "\" branch from repository:" + repositoryUri, 500)
                    )
            );
        }
    }

    private String preparePathForClonedRepository(String repositoryUri, String targetPath) {
        return Paths.get(targetPath, replaceSlashes(repositoryUri), LocalDateTime.now().toString()).toString();
    }

    private String replaceSlashes(String path) {
        return path.replace("/", "_").replace("\\", "_");
    }
}
