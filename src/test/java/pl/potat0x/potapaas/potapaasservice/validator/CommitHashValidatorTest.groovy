package pl.potat0x.potapaas.potapaasservice.validator

import spock.lang.Specification

class CommitHashValidatorTest extends Specification {

    def "should accept valid commit hashes"() {
        expect:
        CommitHashValidator.validate(commitHash).isValid()
        CommitHashValidator.validate(commitHash, false).isValid()
        CommitHashValidator.validate(commitHash, true).isValid()

        where:
        commitHash << [
                "2836e65",
                "2836e65f",
                "2836e65ffbf267a6092a8268333ebc966289a256",
                "0123456789abcdefffffffffffffffffffffffff",
        ]
    }

    def "should reject invalid commit hashes"() {
        expect:
        CommitHashValidator.validate(commitHash).isInvalid()
        CommitHashValidator.validate(commitHash, false).isInvalid()

        where:
        commitHash << [
                null,
                "",
                "test",
                "2836e6",
                "g836e65ffbf267a6092a8268333ebc966289a256",
                "z123456789abcdefffffffffffffffffffffffff",
        ]
    }

    def "should accept null when commit hash is nullable"() {
        expect:
        CommitHashValidator.validate(comitHash, true).isValid()

        where:
        comitHash = null
    }
}
