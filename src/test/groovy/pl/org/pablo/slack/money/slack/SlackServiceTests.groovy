package pl.org.pablo.slack.money.slack

import pl.org.pablo.slack.money.graph.PayRelationship
import pl.org.pablo.slack.money.graph.UserEntity
import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.money.BalanceDto
import pl.org.pablo.slack.money.money.MoneyService
import pl.org.pablo.slack.money.user.UserService
import spock.lang.Specification

import java.time.LocalDateTime

class SlackServiceTests extends Specification {

    def moneyService = Mock(MoneyService)
    def userService = Mock(UserService)
    def chatService = Mock(ChatService)
    def slackUserService = Mock(SlackUserService)

    def cut = new SlackServiceImpl(moneyService, userService, chatService, slackUserService)

    def "When adding new valid payment both MoneyService and ChatService should be notified about it"() {
        given:
        def command = "<@W1|Test> 10 desc"
        when:
        cut.add(new SlackRequest([text: command, user_id: "W2", user_name: "name"]))
        then:
        1 * moneyService.addMoney(new AddDto("W2", "W1", 10, "desc"))
        1 * chatService.sendMessage("W1", _)
        0 * _
    }

    def "When sending wrong command when adding new payment IllegalArgumentException should be thrown"() {
        given:
        def command = "wrong command"
        when:
        cut.add(new SlackRequest([text: command]))
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
