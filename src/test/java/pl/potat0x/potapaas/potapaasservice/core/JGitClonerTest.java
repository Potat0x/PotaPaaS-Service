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

public class JGitClonerTest {

    private static String temporaryReposDirectory;
    private static JGitCloner cloner;

    @BeforeClass
    public static void createTemporaryReposDirectory() throws Exception {
        temporaryReposDirectory = Files.createTempDirectory("potapaas_test_dir").toAbsolutePath().toString();
        cloner = new JGitCloner();
    }

    @Test
    public void shouldClonePublicRepoAndCheckoutToGivenCommit() {
        String repoUrl = "https://github.com/potat0x/potapaas-test-cases";

        String clonedRepoDir = cloner.cloneBranch(repoUrl, "webhook_push_event", temporaryReposDirectory).get();

        Condition<String> containsGitRepo = new Condition<>(
                path -> Paths.get(path, ".git").toFile().isDirectory(),
                "contains .git directory"
        );
        assertThat(clonedRepoDir).satisfies(containsGitRepo);
        assertThat(cloner.getHashOfCurrentCommit(clonedRepoDir).get()).isEqualTo("f45320e1058693c832a17e3d922c7b0444adbe93");

        cloner.checkout(clonedRepoDir, "199da24").get();
        assertThat(cloner.getHashOfCurrentCommit(clonedRepoDir).get()).isEqualTo("199da24b6518931c41f373bc05f9faa322acbce6");
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldGetCloneErrorDueToInvalidBranchName() {
        String repoUrl = "https://github.com/potat0x/potapaas-test-cases";

        cloner.cloneBranch(repoUrl, "this_branch_does_not_exist", temporaryReposDirectory).get();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldGetCloneErrorDueToInvalidRepoUrl() {
        String invalidRepoUrl = "https://github.com/potat0x/invalid-repo-url";

        cloner.cloneBranch(invalidRepoUrl, "master", temporaryReposDirectory).get();
    }

    @AfterClass
    public static void deleteTemporaryReposDirectory() throws IOException {
        FileSystemUtils.deleteRecursively(Path.of(temporaryReposDirectory));
    }
}
