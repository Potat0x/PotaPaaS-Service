package pl.potat0x.potapaas.potapaasservice.datastore;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@ToString
@EqualsAndHashCode
public final class DatastoreResponseDto {
    private final String uuid;
    private final String name;
    private final DatastoreType type;
    private final Set<String> attachedApps;

    @JsonCreator
    public DatastoreResponseDto(@JsonProperty("uuid") String uuid, @JsonProperty("name") String name, @JsonProperty("type") DatastoreType type, @JsonProperty("attachedApps") Set<String> attachedApps) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
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

    public Set<String> getAttachedApps() {
        return attachedApps;
    }
}