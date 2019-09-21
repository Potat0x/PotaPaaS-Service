package pl.potat0x.potapaas.potapaasservice.utils

import spock.lang.Specification

class NameValidatorTest extends Specification {

    def "should accept valid names"() {
        expect:
        NameValidator.validate(name).isValid()

        where:
        name << [
                "az",
                "a-z",
                "a-b-c",
                "abc",
                "12",
                "a-123-z",
        ]
    }

    def "should reject invalid names"() {
        expect:
        NameValidator.validate(name).isInvalid()

        where:
        name << [
                null,
                "",
                "-",
                "--",
                " ",
                "  ",
                " a",
                "a ",
                "-aa",
                "aa-",
                "-aa-",
                "--",
                "aa--a",
        ]
    }
}
