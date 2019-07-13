package pl.potat0x.potapaas.potapaasservice.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
final class AppRequestDto {
    private final String name;
    private final String type;
    private final String sourceRepoUrl;
    private final String sourceBranchName;

    @JsonCreator
    public AppRequestDto(@JsonProperty("name") String name,
                         @JsonProperty("type") String type,
                         @JsonProperty("sourceRepoUrl") String sourceRepoUrl,
                         @JsonProperty("sourceBranchName") String sourceBranchName) {
        this.name = name;
        this.type = type;
        this.sourceRepoUrl = sourceRepoUrl;
        this.sourceBranchName = sourceBranchName;
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
}
