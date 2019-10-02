package pl.potat0x.potapaas.potapaasservice.validator


import spock.lang.Specification

class UuidValidatorTest extends Specification {

    def "should accept valid UUIDs"() {
        expect:
        UuidValidator.checkIfValid(uuid)

        where:
        uuid << [
                "bb7353e5-f0be-4168-8e0c-ad864b02a753",
                "F0002737-5494-49Bf-9a9b-9337A8b54987",
                "bb7353e5-f0be-4168-8e0c-ad864b02a753".toUpperCase(),
        ]
    }

    def "should reject invalid UUIDs"() {
        expect:
        !UuidValidator.checkIfValid(uuid)

        where:
        uuid << [
                null,
                "",
                "-",
                "------------------------------------",
                "bb7353e5-f0be-4168-8e0c-ad864b02a",
                "bb7353e5-f0be-4168-e0c-ad864b02a753",
                "bb7353e5-fbe-4168-8e0c-ad864b02a753",
                "bb7353e5-f0be-4168-8e0c-ad864b02a75333",
                "bb7353e5-f0be-4168-8e0c-ad864b02a7533",
                "bb7353e-5f0be-4168-8e0c-ad864b02a753",
                "bb7353e5-f0be4-168-8e0c-ad864b02a753",
                "bb7353e5-f0be-416-88e0c-ad864b02a753",
                "bb7353e5-f0be-4168-8e0ca-d864b02a753",
                "bb7353e5-f0be-4168-8e0c-ad864b02a7--",
                "bb7353e5af0bea4168a8e0caad864b02a753",
                "gb7353e5-f0be-4168-8e0c-ad864b02a753",
                "zb7353e5-f0be-4168-8e0c-ad864b02a753"
        ]
    }
}
