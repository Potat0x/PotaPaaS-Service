package pl.potat0x.potapaas.potapaasservice;

import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.core.GitCloner;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.io.IOException;
import java.nio.file.Files;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

public class FakeGitCloner implements GitCloner {
    @Override
    public Either<ErrorMessage, String> cloneBranch(String repositoryUri, String branchName, String targetPath) {
        try {
            return Either.right(Files.createTempDirectory("fake_git_cloner").toString());
        } catch (IOException e) {
            e.printStackTrace();
            return Either.left(message("FakeGitCloner: " + e.getMessage(), 500));
        }
    }
}
