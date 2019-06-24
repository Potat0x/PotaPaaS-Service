package pl.potat0x.potapaas.potapaasservice.util;

import io.vavr.control.Either;
import org.assertj.core.api.Condition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class GitClonerTest {

    private static Path temporaryReposDirectory;

    @BeforeClass
    public static void createTemporaryReposDirectory() throws IOException {
        temporaryReposDirectory = Files.createTempDirectory("potapaas_test_dir");
    }

    @Test
    public void shouldClonePublicRepo() {
        GitCloner cloner = new GitCloner(temporaryReposDirectory.toAbsolutePath().toString());
        String repoLink = "https://github.com/spotify/comet-core";

        Condition<String> containsGitRepo = new Condition<>(
                path -> Paths.get(path, ".git").toFile().isDirectory(),
                "contains .git directory"
        );

        Either<String, String> result = cloner.cloneBranch(repoLink, "master");

        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse("/dev/null")).satisfies(containsGitRepo);
    }

    @AfterClass
    public static void deleteTemporaryReposDirectory() throws IOException {
        FileSystemUtils.deleteRecursively(temporaryReposDirectory);
    }
}
