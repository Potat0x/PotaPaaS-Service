package pl.potat0x.potapaas.potapaasservice.core;

import io.vavr.control.Either;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.CaseBuilderStart.exception;
import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

public final class GitCloner {

    private final String targetPath;

    static Either<ErrorMessage, GitCloner> create(Path targetPath) {
        return create(targetPath.toString());
    }

    static Either<ErrorMessage, GitCloner> create(String targetPath) {
        if (targetPath != null && new File(targetPath).isDirectory()) {
            return Either.right(new GitCloner(targetPath));
        } else {
            return Either.left(CoreErrorMessage.SERVER_ERROR);
        }
    }

    Either<ErrorMessage, String> cloneBranch(String repositoryUri, String branchName) {
        System.err.println("___cloneBranch start");
        try {
            System.err.println("___cloneBranch start a");
            String clonedRepoDirectory = preparePathForClonedRepository(repositoryUri);
            System.err.println("___cloneBranch start b");
            Git repo = Git.cloneRepository()
                    .setURI(repositoryUri)
                    .setBranch(branchName)
                    .setDirectory(new File(clonedRepoDirectory))
                    .call();
            repo.close();//todo: trywith
            System.err.println("___branchList().call()");
            if (repo.branchList().call().size() > 0) {
                System.err.println("___cloneBranch end right" + clonedRepoDirectory);
                return Either.right(clonedRepoDirectory);
            }

            System.err.println("___cloneBranch end left ");
            return Either.left(message("branch \"" + branchName + "\" not found in repository: " + repositoryUri, 404));
        } catch (Exception e) {
            System.err.println("___cloneBranch end left map");
            return ExceptionMapper.map(e).of(
                    exception(GitAPIException.class, InvalidRemoteException.class).to(
                            message("Error while cloning \"" + branchName + "\" branch from repository:" + repositoryUri, 500)
                    )
            );
        }
    }

    private GitCloner(String targetPath) {
        this.targetPath = targetPath;
    }

    private String preparePathForClonedRepository(String repositoryUri) {
        return Paths.get(targetPath, replaceSlashes(repositoryUri), LocalDateTime.now().toString()).toString();
    }

    private String replaceSlashes(String path) {
        return path.replace("/", "_").replace("\\", "_");
    }
}
