package pl.potat0x.potapaas.potapaasservice.utils

import spock.lang.Specification

class NameValidatorTest extends Specification {

    def "should accept valid names"() {
        expect:
        NameValidator.validate(name, "app name").isValid()
        NameValidator.validate(name, "app name", false).isValid()
        NameValidator.validate(name, "app name", true).isValid()

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
        NameValidator.validate(name, "app name").isInvalid()
        NameValidator.validate(name, "app name", false).isInvalid()

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

    def "should accept null when name is nullable"() {
        expect:
        NameValidator.validate(name, "app name", true).isValid()

        where:
        name = null
    }
}
