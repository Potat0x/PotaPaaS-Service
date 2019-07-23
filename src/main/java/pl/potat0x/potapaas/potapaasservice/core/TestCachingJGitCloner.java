package pl.potat0x.potapaas.potapaasservice.core;

import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

/*
 * FOR TESTS ONLY
 *
 * Each pair (repositoryUri, branchName) has own, constant temp directory
 * Branch is cloned only if corresponding temp directory does not contain git repo yet
 * It does not check, if repository found in temp directory is up to date
 * */
public class TestCachingJGitCloner extends JGitCloner {

    private final String tempDirPrefix;

    public TestCachingJGitCloner() {
        this("");
    }

    public TestCachingJGitCloner(String tempDirPrefix) {
        this.tempDirPrefix = tempDirPrefix != null ? tempDirPrefix : "";
    }

    @Override
    public Either<ErrorMessage, String> cloneBranch(String repositoryUri, String branchName, String dummyTargetPath) {
        final String tempDirName = prepareTempDirName(repositoryUri, branchName);
        String targetPath;
        try {
            targetPath = createTempDirectoryIfNotExists(tempDirName);
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(message("cannot create temp directory with name " + tempDirName, 500));
        }

        if (!checkIfRepoIsAlreadyCloned(targetPath)) {
            super.clone(repositoryUri, branchName, targetPath);
        }

        return Either.right(targetPath);
    }

    private String prepareTempDirName(String repositoryUri, String branchName) {
        return tempDirPrefix + "_" + branchName + "__" + Arrays
                .stream(repositoryUri.split("/"))
                .sorted(Collections.reverseOrder())
                .reduce((a, b) -> a + "_" + b).get();
    }

    private String createTempDirectoryIfNotExists(String tempDirName) throws IOException {
        Path temporaryReposDirectory = Path.of(System.getProperty("java.io.tmpdir"), tempDirName);
        if (!Files.isDirectory(temporaryReposDirectory)) {
            Files.createDirectory(temporaryReposDirectory);
        }
        return temporaryReposDirectory.toString();
    }

    private boolean checkIfRepoIsAlreadyCloned(String path) {
        return Paths.get(path, ".git").toFile().isDirectory();
    }
}
