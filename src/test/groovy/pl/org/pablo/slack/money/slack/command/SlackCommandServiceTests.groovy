package pl.org.pablo.slack.money.slack.command

import kotlin.collections.CollectionsKt
import pl.org.pablo.slack.money.graph.PayRelationship
import pl.org.pablo.slack.money.graph.UserEntity
import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.money.BalanceDto
import pl.org.pablo.slack.money.money.MoneyService
import pl.org.pablo.slack.money.slack.SlackMoneyMergerImpl
import pl.org.pablo.slack.money.slack.SlackUserService
import pl.org.pablo.slack.money.slack.parser.AddArgument
import pl.org.pablo.slack.money.slack.parser.AddSingleArgument
import pl.org.pablo.slack.money.slack.parser.ArgumentParserService
import pl.org.pablo.slack.money.user.UserService
import spock.lang.Specification

import java.time.LocalDateTime

class SlackCommandServiceTests extends Specification {

    def moneyService = Mock(MoneyService)
    def userService = Mock(UserService)
    def slackUserService = Mock(SlackUserService)
    def argumentParser = Mock(ArgumentParserService)
    def slackMoneyMerger = new SlackMoneyMergerImpl()

    def cut = new SlackCommandServiceImpl(moneyService, userService, slackUserService, argumentParser, slackMoneyMerger)

    def "When command is valid and there is a field return proper Slack interactive message"() {
        given:
        def desc = "desc"
        def from = "from"
        def to = "to"
        def value = 10.00
        def payment = new AddSingleArgument([to], value, [] as Set)
        def addArg = new AddArgument([payment], desc)
        argumentParser.parse(_, _) >> addArg
        slackMoneyMerger.mergeUserIntoSequence([payment], from, desc) >> CollectionsKt.asSequence([new AddDto(from, to, value, desc)])
        slackUserService.getUserName(_) >> { args -> args[0] }
        when:
        def result = cut.add(new SlackRequest([text: "_", user_id: from]))
        then:
        !result.attachments.isEmpty()
        result.attachments[0].fields[0].value == value.toString()
        result.attachments[0].fields[0].title == to
        result.attachments[0].actions[0].value == "_"
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
        result.text == "All good my dear friend!"
    }

    def "When user don't have empty balance send proper information about balance"() {
        given:
        moneyService.getBalance(_) >> [new BalanceDto("W1", 10), new BalanceDto("W2", -10)]
        slackUserService.getUserName(_) >> { args -> args[0] }
        when:
        def result = cut.getBalance(new SlackRequest([user_id: "id"]))
        then:
        result.attachments[0].fields.size() == 2
    }

    def "When fetching empty payment history result should be empty"() {
        given:
        userService.getOrCreate(_) >> new UserEntity("W1", [], [], [], [], null)
        when:
        def result = cut.getPaymentHistory(new SlackRequest([user_id: "id"]))
        then:
        result.attachments[0].fields.empty
    }

    def "When fetching payment history result should have not empty string"() {
        given:
        def p1 = new PayRelationship(new UserEntity(), new UserEntity(), 10, "desc", LocalDateTime.now(), null)
        def p2 = new PayRelationship(new UserEntity(), new UserEntity(), 10, "desc", LocalDateTime.now(), null)
        userService.getOrCreate(_) >> new UserEntity("W1", [p1], [p2], [], [], null)
        when:
        def result = cut.getPaymentHistory(new SlackRequest([user_id: "id"]))
        then:
        !result.attachments[0].fields.empty
    }

}
