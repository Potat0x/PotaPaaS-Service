package pl.potat0x.potapaas.potapaasservice.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class UuidAndNameResponseDto {
    private final String uuid;
    private final String name;

    @JsonCreator
    public UuidAndNameResponseDto(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}
