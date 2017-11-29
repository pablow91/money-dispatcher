package pl.org.pablo.slack.money.slack

import pl.org.pablo.slack.money.graph.PayRelationship
import pl.org.pablo.slack.money.graph.UserEntity
import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.money.BalanceDto
import pl.org.pablo.slack.money.money.MoneyService
import pl.org.pablo.slack.money.slack.parser.AddArgument
import pl.org.pablo.slack.money.slack.parser.AddSingleArgument
import pl.org.pablo.slack.money.slack.parser.ArgumentParserService
import pl.org.pablo.slack.money.user.UserService
import spock.lang.Specification

import java.time.LocalDateTime

class SlackServiceTests extends Specification {

    def moneyService = Mock(MoneyService)
    def userService = Mock(UserService)
    def chatService = Mock(ChatService)
    def slackUserService = Mock(SlackUserService)
    def argumentParser = Mock(ArgumentParserService)

    def cut = new SlackServiceImpl(moneyService, userService, chatService, slackUserService, argumentParser)

    def "When adding new valid payment both MoneyService and ChatService should be notified about it"() {
        given:
        argumentParser.parse(_, _) >> new AddArgument([new AddSingleArgument(["W1"], 10, [] as Set)], "desc")
        when:
        cut.add(new SlackRequest([text: "", user_id: "W2", user_name: "name"]))
        then:
        1 * moneyService.addMoney(new AddDto("W2", "W1", 10.00, "desc"))
        1 * chatService.sendMessage("W1", _)
    }

    def "When two users are part of the payment both should have got new payment and receive message"() {
        given:
        argumentParser.parse(_, _) >> new AddArgument([new AddSingleArgument(["W1", "W3"], 10, [] as Set)], "desc")
        when:
        cut.add(new SlackRequest([text: "", user_id: "W2", user_name: "name"]))
        then:
        1 * moneyService.addMoney(new AddDto("W2", "W1", 10.00, "desc"))
        1 * moneyService.addMoney(new AddDto("W2", "W3", 10.00, "desc"))
        1 * chatService.sendMessage("W1", _)
        1 * chatService.sendMessage("W3", _)
    }

    def "When adding user twice in one payment merge his money"() {
        given:
        argumentParser.parse(_, _) >> new AddArgument(
                [new AddSingleArgument(["W1"], 10, [] as Set), new AddSingleArgument(["W1"], 20, [] as Set)],
                "desc")
        when:
        cut.add(new SlackRequest([text: "", user_id: "W2", user_name: "name"]))
        then:
        1 * moneyService.addMoney(new AddDto("W2", "W1", 30.00, "desc"))
        1 * chatService.sendMessage("W1", _)
    }

    def "When splitting in payment is enabled for 2 people value in their new payments should be split in two"() {
        given:
        argumentParser.parse(_, _) >> new AddArgument([new AddSingleArgument(["W1", "W3"], 10, ["eq"] as Set)], "desc")
        when:
        cut.add(new SlackRequest([text: "", user_id: "W2", user_name: "name"]))
        then:
        1 * moneyService.addMoney(new AddDto("W2", "W1", 5.00, "desc"))
        1 * moneyService.addMoney(new AddDto("W2", "W3", 5.00, "desc"))
        1 * chatService.sendMessage("W1", _)
        1 * chatService.sendMessage("W3", _)
    }

    def "When sending wrong command when adding new payment IllegalArgumentException should be thrown"() {
        given:
        argumentParser.parse(_, _) >> { throw new IllegalArgumentException() }
        when:
        cut.add(new SlackRequest([text: ""]))
        then:
        thrown IllegalArgumentException
    }

    def "When user has clear balance proper message should be send"() {
        given:
        moneyService.getBalance(_) >> []
        when:
        def result = cut.getBalance(new SlackRequest([user_id: "id"]))
        then:
        result == "All good my dear friend!"
    }

    def "When user don't have empty balance send proper information about balance"() {
        given:
        moneyService.getBalance(_) >> [new BalanceDto("W1", 10), new BalanceDto("W2", -10)]
        slackUserService.getUserName(_) >> { args -> args[0] }
        when:
        def result = cut.getBalance(new SlackRequest([user_id: "id"]))
        then:
        result == "W1 10\nW2 -10"
    }

    def "When fetching empty payment history result should be empty"() {
        given:
        userService.getOrCreate(_) >> new UserEntity("W1", [], [], [], [], null)
        when:
        def result = cut.getPaymentHistory(new SlackRequest([user_id: "id"]))
        then:
        result.empty
    }

    def "When fetching payment history result should have not empty string"() {
        given:
        def p1 = new PayRelationship(new UserEntity(), new UserEntity(), 10, "desc", LocalDateTime.now(), null)
        def p2 = new PayRelationship(new UserEntity(), new UserEntity(), 10, "desc", LocalDateTime.now(), null)
        userService.getOrCreate(_) >> new UserEntity("W1", [p1], [p2], [], [], null)
        when:
        def result = cut.getPaymentHistory(new SlackRequest([user_id: "id"]))
        then:
        !result.empty
    }

}
