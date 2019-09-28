package pl.potat0x.potapaas.potapaasservice.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public final class AppRequestDto {
    private final String name;
    private final String type;
    private final String sourceRepoUrl;
    private final String sourceBranchName;
    private final String commitHash;
    private final String datastoreUuid;

    @JsonCreator
    public AppRequestDto(@JsonProperty("name") String name,
                         @JsonProperty("type") String type,
                         @JsonProperty("sourceRepoUrl") String sourceRepoUrl,
                         @JsonProperty("sourceBranchName") String sourceBranchName,
                         @JsonProperty("commitHash") String commitHash,
                         @JsonProperty("datastoreUuid") String datastoreUuid) {
        this.name = name;
        this.type = type;
        this.sourceRepoUrl = sourceRepoUrl;
        this.sourceBranchName = sourceBranchName;
        this.commitHash = commitHash;
        this.datastoreUuid = datastoreUuid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSourceRepoUrl() {
        return sourceRepoUrl;
    }

    public String getSourceBranchName() {
        return sourceBranchName;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getDatastoreUuid() {
        return datastoreUuid;
    }
}
