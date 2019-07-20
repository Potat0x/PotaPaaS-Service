package pl.potat0x.potapaas.potapaasservice;

import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.core.GitCloner;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.nio.file.Paths;

public class FakeGitCloner implements GitCloner {
    @Override
    public Either<ErrorMessage, String> cloneBranch(String repositoryUri, String branchName, String targetPath) {
        return Either.right(Paths.get(targetPath, "test").toString());
    }
}
