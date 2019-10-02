package pl.potat0x.potapaas.potapaasservice.datastore;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@ToString
@EqualsAndHashCode
public final class DatastoreResponseDto {
    private final String uuid;
    private final String name;
    private final DatastoreType type;
    private final LocalDateTime createdAt;
    private final String status;
    private final Set<String> attachedApps;

    @JsonCreator
    public DatastoreResponseDto(@JsonProperty("uuid") String uuid,
                                @JsonProperty("name") String name,
                                @JsonProperty("type") DatastoreType type,
                                @JsonProperty("createdAt") LocalDateTime createdAt,
                                @JsonProperty("status") String status,
                                @JsonProperty("attachedApps") Set<String> attachedApps
    ) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
        this.createdAt = createdAt;
        this.status = status;
        this.attachedApps = attachedApps;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public DatastoreType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public Set<String> getAttachedApps() {
        return attachedApps;
    }
}