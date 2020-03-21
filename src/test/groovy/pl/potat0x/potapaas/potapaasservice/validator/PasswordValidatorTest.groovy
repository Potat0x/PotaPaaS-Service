package pl.potat0x.potapaas.potapaasservice.validator

import org.apache.commons.lang.StringUtils
import spock.lang.Specification

class PasswordValidatorTest extends Specification {
    def "should accept valid passwords"() {
        expect:
        PasswordValidator.validate(password).isValid()

        where:
        password << [
                "aB!45678",
                "(23)5s&E",
                "Long password            !1",
        ]
    }

    def "should reject invalid passwords"() {
        expect:
        PasswordValidator.validate(password).isInvalid()

        where:
        password << [
                null,
                "",
                "Abc123!",
                "aabbccdd",
                "AABBCCDD",
                "AABBccdd",
                "12345678",
                "Abc12345",
                "aB!45678" + StringUtils.repeat("_", 130),
        ]
    }
}
