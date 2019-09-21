package pl.potat0x.potapaas.potapaasservice.utils

import spock.lang.Specification

enum TestEnum {
    TYPE_1,
    TYPE_2,
    TYPE_3,
}

class EnumValidatorTest extends Specification {

    def "should accept valid type"() {
        expect:
        EnumValidator.checkIfEnumContainsConstant(enumval, TestEnum.class, "test enum type").isValid()

        where:
        enumval << ["TYPE_1", "TYPE_2", "TYPE_3"]
    }

    def "should reject invalid type"() {
        expect:
        EnumValidator.checkIfEnumContainsConstant(enumval, TestEnum.class, "test enum type").isInvalid()

        where:
        enumval << [null, "", "TYPE_", "type_1"]
    }
}
