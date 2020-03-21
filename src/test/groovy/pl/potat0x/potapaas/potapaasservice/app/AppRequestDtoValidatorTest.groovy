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

                validDto().withCommitHash(null),
                validDto().withCommitHash("0123def"),
                validDto().withCommitHash("d501e921c6764a516101a32cda3f0ccebe5946cc"),

                validDto().withDatastoreUuid(null),
                validDto().withDatastoreUuid(UUID.randomUUID().toString()),
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

                validDto().withCommitHash("a"),
                validDto().withCommitHash("d501e921c6764a516101a32cda3f0ccebe5946cca"),

                validDto().withDatastoreUuid(""),
                validDto().withDatastoreUuid("invalid_datastore_name"),
        ]
    }

    private static AppRequestDtoBuilder validDto() {
        return new AppRequestDtoBuilder()
                .withName("app-name-test123")
                .withType("NODEJS")
                .withSourceRepoUrl("https://github.com/Potat0x/potapaas-test-cases")
                .withSourceBranchName("nodejs_test_ok")
                .withDatastoreUuid(UUID.randomUUID().toString())
    }
}
