package pl.potat0x.potapaas.potapaasservice.datastore;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
final class DatastoreDto {
    private final String name;
    private final String type;

    @JsonCreator
    public DatastoreDto(@JsonProperty("name") String name, @JsonProperty("type") String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
