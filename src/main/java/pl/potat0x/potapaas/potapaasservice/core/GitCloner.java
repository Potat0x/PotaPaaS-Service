package pl.potat0x.potapaas.potapaasservice.core;

import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

public interface GitCloner {
    Either<ErrorMessage, String> cloneBranch(String repositoryUri, String branchName, String targetPath);

    Either<ErrorMessage, String> checkout(String repositoryDir, String commitHash);

    Either<ErrorMessage, String> getHashOfCurrentCommit(String repositoryDir);
}
