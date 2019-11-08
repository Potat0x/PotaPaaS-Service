package pl.potat0x.potapaas.potapaasservice.webhook

import spock.lang.Specification

class HmacVerifierTest extends Specification {

    def "should verify message authenticity"() {

        expect:
        new HmacVerifier(message, sha1HexDigest).isMessageAuthentic(secret) == expectedResult

        where:
        message          | sha1HexDigest                              | secret       | expectedResult
        "test message"   | "bb3f1adc117ea0ed159d8e6baafb9dffe48c615a" | "test key"   | true
        "test message 2" | "b9bf5a4f63ffb89a5a08b5643e42fe499c778982" | "test key 2" | true
        "test message 3" | "bb3f1adc117ea0ed159d8e6baafb9dffe48c615a" | "test key 2" | false
        "test message 2" | "c9bf5a4f63ffb89a5a08b5643e42fe499c778982" | "test key 2" | false
    }
}
