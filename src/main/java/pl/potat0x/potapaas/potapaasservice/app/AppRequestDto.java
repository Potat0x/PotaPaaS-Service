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
    private final String datastoreName;

    @JsonCreator
    public AppRequestDto(@JsonProperty("name") String name,
                         @JsonProperty("type") String type,
                         @JsonProperty("sourceRepoUrl") String sourceRepoUrl,
                         @JsonProperty("sourceBranchName") String sourceBranchName,
                         @JsonProperty("datastoreName") String datastoreName) {
        this.name = name;
        this.type = type;
        this.sourceRepoUrl = sourceRepoUrl;
        this.sourceBranchName = sourceBranchName;
        this.datastoreName = datastoreName;
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

    public String getDatastoreName() {
        return datastoreName;
    }
}
