package pl.potat0x.potapaas.potapaasservice.util;

import io.vavr.control.Either;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public final class GitCloner {

    private final String targetPath;

    GitCloner(String targetPath) {
        if (new File(targetPath).isDirectory()) {
            this.targetPath = targetPath;
        } else {
            throw new IllegalArgumentException(targetPath + " is not directory");
        }
    }

    Either<String, String> cloneBranch(String repositoryUri, String branchName) {

        String clonedRepoDirectory = preparePathForClonedRepository(repositoryUri);
        try {
            Git repo = Git.cloneRepository()
                    .setURI(repositoryUri)
                    .setBranch(branchName)
                    .setDirectory(new File(clonedRepoDirectory))
                    .call();

            if (repo.branchList().call().size() > 0) {
                return Either.right(clonedRepoDirectory);
            }

            return Either.left("branch \"" + branchName + "\" not found in " + repositoryUri);
        } catch (GitAPIException e) {
            e.printStackTrace();
            return Either.left(e.getMessage());
        }
    }

    private String preparePathForClonedRepository(String repositoryUri) {
        String normalizedPath = replaceSlashes(repositoryUri);
        return Paths.get(targetPath, normalizedPath, LocalDateTime.now().toString()).toString();
    }

    private String replaceSlashes(String path) {
        return path.replace("/", "_").replace("\\", "_");
    }
}
