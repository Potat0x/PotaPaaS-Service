package pl.potat0x.potapaas.potapaasservice.core;

import org.assertj.core.api.Condition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class GitClonerTest {

    private static Path temporaryReposDirectory;
    private static GitCloner cloner;

    @BeforeClass
    public static void createTemporaryReposDirectory() throws Exception {
        temporaryReposDirectory = Files.createTempDirectory("potapaas_test_dir");
        cloner = GitCloner.create(temporaryReposDirectory.toAbsolutePath()).get();
    }

    @Test
    public void shouldClonePublicRepo() {
        String repoUrl = "https://github.com/spotify/comet-core";

        String clonedRepoDir = cloner.cloneBranch(repoUrl, "master").get();

        Condition<String> containsGitRepo = new Condition<>(
                path -> Paths.get(path, ".git").toFile().isDirectory(),
                "contains .git directory"
        );
        assertThat(clonedRepoDir).satisfies(containsGitRepo);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldGetCloneErrorDueToInvalidBranchName() {
        String repoUrl = "https://github.com/spotify/comet-core";

        cloner.cloneBranch(repoUrl, "this_branch_does_not_exist").get();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldGetCloneErrorDueToInvalidRepoUrl() {
        String invalidRepoUrl = "https://github.com/potat0x/invalid-repo-url";

        cloner.cloneBranch(invalidRepoUrl, "master").get();
    }

    @AfterClass
    public static void deleteTemporaryReposDirectory() throws IOException {
        FileSystemUtils.deleteRecursively(temporaryReposDirectory);
    }
}
