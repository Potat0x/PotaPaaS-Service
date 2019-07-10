package pl.potat0x.potapaas.potapaasservice.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "AppRequestDto{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", sourceRepoUrl='" + sourceRepoUrl + '\'' +
                ", sourceBranchName='" + sourceBranchName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppRequestDto that = (AppRequestDto) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(sourceRepoUrl, that.sourceRepoUrl) &&
                Objects.equals(sourceBranchName, that.sourceBranchName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, sourceRepoUrl, sourceBranchName);
    }
}
