package pl.potat0x.potapaas.potapaasservice.app

import io.vavr.control.Validation
import spock.lang.Specification

class AppRequestDtoValidatorTest extends Specification {
    private AppRequestDtoValidator validator = new AppRequestDtoValidator()

    def "should accept valid DTO"() {
        when: "request DTO is valid"
        Validation validation = validator.validate(requestDto.build())

        then: "result should be valid"
        validation.isValid()

        where:
        requestDto << [
                validDto(),
                validDto().withName("aa"),
                validDto().withName("22"),
                validDto().withName("2-2"),
                validDto().withName("2-2-a-a"),

                validDto().withType("NODEJS"),

                validDto().withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases"),

                validDto().withSourceBranchName("master"),

                validDto().withDatastoreName(null),
                validDto().withDatastoreName("datastore-123"),
        ]
    }

    def "should detect that request DTO is invalid"() {
        when: "request DTO is invalid"
        Validation validation = validator.validate(requestDto.build())

        then: "result should be invalid"
        validation.isInvalid()

        where:
        requestDto << [
                validDto().withName(null),
                validDto().withName(""),
                validDto().withName("a"),
                validDto().withName("a--a"),

                validDto().withType(null),
                validDto().withType("invalid_type"),

                validDto().withSourceRepoUrl(null),
                validDto().withSourceRepoUrl(""),
                validDto().withSourceRepoUrl("invalid-url"),

                validDto().withSourceBranchName(null),
                validDto().withSourceBranchName(""),

                validDto().withDatastoreName(""),
                validDto().withDatastoreName("invalid_datastore_name"),
        ]
    }

    private static AppRequestDtoBuilder validDto() {
        return new AppRequestDtoBuilder()
                .withName("app-name-test123")
                .withType("NODEJS")
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok")
                .withDatastoreName("datastore-name")
    }
}
