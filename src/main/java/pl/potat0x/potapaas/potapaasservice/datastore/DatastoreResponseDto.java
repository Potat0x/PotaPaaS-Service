package pl.potat0x.potapaas.potapaasservice.datastore;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
final class DatastoreResponseDto {
    private final String uuid;
    private final String name;
    private final String type;

    @JsonCreator
    public DatastoreResponseDto(@JsonProperty("uuid") String uuid, @JsonProperty("name") String name, @JsonProperty("type") String type) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}