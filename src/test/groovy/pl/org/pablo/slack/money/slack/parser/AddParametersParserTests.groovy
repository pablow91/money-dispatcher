package pl.org.pablo.slack.money.slack.parser

import spock.lang.Specification
import spock.lang.Unroll

class AddParametersParserTests extends Specification {

    def parser = new ArgumentParserServiceImpl()
    def cut = new AddArgumentParser()

    def "One user, no options, with value, no desc"() {
        given:
        def command = "<@W1> 10"
        when:
        def res = parser.parse(cut, command)
        then:
        res.getPayments().size() == 1
        res.getPayments()[0].options.empty
        res.getPayments()[0].users == ["W1"]
        res.getPayments()[0].value == 10
        res.getDescription() == null
    }

    def "One user, one options, with value, with desc"() {
        given:
        def command = "<@W1|U1> -a 10 desc desc"
        when:
        def res = parser.parse(cut, command)
        then:
        res.getPayments().size() == 1
        res.getPayments()[0].options == (["a"] as Set)
        res.getPayments()[0].users == ["W1"]
        res.getPayments()[0].value == 10
        res.getDescription() == "desc desc"
    }

    def "One user, one options (at begging), with value, with desc"() {
        given:
        def command = "-a <@W1|U1> 10 desc"
        when:
        def res = parser.parse(cut, command)
        then:
        res.getPayments().size() == 1
        res.getPayments()[0].options == (["a"] as Set)
        res.getPayments()[0].users == ["W1"]
        res.getPayments()[0].value == 10
        res.getDescription() == "desc"
    }

    def "Two users, one options, with one value, with desc"() {
        given:
        def command = "<@W1|U1> <@W2|U2> -a 10 desc"
        when:
        def res = parser.parse(cut, command)
        then:
        res.getPayments().size() == 1
        res.getPayments()[0].options == (["a"] as Set)
        res.getPayments()[0].users == ["W1", "W2"]
        res.getPayments()[0].value == 10
        res.getDescription() == "desc"
    }

    def "Two pairs - two users, one options, with value, with desc"() {
        given:
        def command = "<@W1|U1> <@W2|U2> -a 10 <@W3|U3> <@W4|U4> -b 20 desc"
        when:
        def res = parser.parse(cut, command)
        then:
        res.getPayments().size() == 2
        res.getPayments()[0].options == (["a"] as Set)
        res.getPayments()[0].users == ["W1", "W2"]
        res.getPayments()[0].value == 10
        res.getPayments()[1].options == (["b"] as Set)
        res.getPayments()[1].users == ["W3", "W4"]
        res.getPayments()[1].value == 20
        res.getDescription() == "desc"
    }

    def "One user, one options, with negative value, with desc"() {
        given:
        def command = "<@W1|U1> -a -10 desc"
        when:
        def res = parser.parse(cut, command)
        then:
        res.getPayments().size() == 1
        res.getPayments()[0].options == (["a"] as Set)
        res.getPayments()[0].users == ["W1"]
        res.getPayments()[0].value == -10
        res.getDescription() == "desc"
    }

    @Unroll
    def "Invalid command (state) - #command"(String command) {
        when:
        parser.parse(cut, command)
        then:
        thrown IllegalStateException
        where:
        command                | _
        "<@W1|U1>"             | _
        "<@W1|U1> 10 <@W2|U2>" | _
    }

    @Unroll
    def "Invalid command (argument) - #command"(String command) {
        when:
        parser.parse(cut, command)
        then:
        thrown IllegalArgumentException
        where:
        command         | _
        "<@W1|U1> desc" | _
        "-a 10"         | _
        "desc"          | _
        "10"            | _
        ""              | _
    }

}
