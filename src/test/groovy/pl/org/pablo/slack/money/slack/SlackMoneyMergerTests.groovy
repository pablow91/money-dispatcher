package pl.org.pablo.slack.money.slack

import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.slack.parser.AddSingleArgument
import spock.lang.Specification

class SlackMoneyMergerTests extends Specification {

    def cut = new SlackMoneyMergerImpl()

    def "When adding new valid payment both MoneyService and ChatService should be notified about it"() {
        when:
        def result = cut.mergeUserIntoSequence([new AddSingleArgument(["W1"], 10, [] as Set)], "W2", "desc").iterator().toList()
        then:
        result.size() == 1
        result.get(0) == new AddDto("W2", "W1", 10.00, "desc")
    }

    def "When two users are part of the payment both should have got new payment and receive message"() {
        when:
        def result = cut.mergeUserIntoSequence([new AddSingleArgument(["W1", "W3"], 10, [] as Set)], "W2", "desc").iterator().toList()
        then:
        result.size() == 2
        result.get(0) == new AddDto("W2", "W1", 10.00, "desc")
        result.get(1) == new AddDto("W2", "W3", 10.00, "desc")
    }

    def "When adding user twice in one payment merge his money"() {
        when:
        def result = cut.mergeUserIntoSequence([new AddSingleArgument(["W1"], 10, [] as Set), new AddSingleArgument(["W1"], 20, [] as Set)],
                "W2", "desc").iterator().toList()
        then:
        result.size() == 1
        result.get(0) == new AddDto("W2", "W1", 30.00, "desc")
    }

    def "When splitting in payment is enabled for 2 people value in their new payments should be split in two"() {
        when:
        def result = cut.mergeUserIntoSequence([new AddSingleArgument(["W1", "W3"], 10, ["eq"] as Set)], "W2", "desc").iterator().toList()
        then:
        result.size() == 2
        result.get(0) == new AddDto("W2", "W1", 5.00, "desc")
        result.get(1) == new AddDto("W2", "W3", 5.00, "desc")
    }

}
