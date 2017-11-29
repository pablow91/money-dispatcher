package pl.org.pablo.slack.money.slack.interpreter

import kotlin.collections.CollectionsKt
import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.money.MoneyService
import pl.org.pablo.slack.money.slack.ChatService
import pl.org.pablo.slack.money.slack.SlackMoneyMerger
import pl.org.pablo.slack.money.slack.SlackUserService
import pl.org.pablo.slack.money.slack.interactive.*
import pl.org.pablo.slack.money.slack.parser.AddArgument
import pl.org.pablo.slack.money.slack.parser.AddSingleArgument
import pl.org.pablo.slack.money.slack.parser.ArgumentParserService
import spock.lang.Specification

class SlackInteractiveMessageServiceTests extends Specification {

    def argumentParserService = Mock(ArgumentParserService)
    def chatService = Mock(ChatService)
    def moneyService = Mock(MoneyService)
    def slackMoneyMerger = Mock(SlackMoneyMerger)
    def slackMoneyService = Mock(SlackUserService)

    def cut = new SlackInteractiveMessageServiceImpl(argumentParserService, chatService, moneyService, slackMoneyMerger, slackMoneyService)

    def team = new Team("", "")
    def channel = new Channel("", "")
    def user = new User("from", "")

    def "Receiving unknown caller id causes IllegalArgumentException"() {
        when:
        cut.parse(new SlackInteractiveDto([], "unknown", team, channel, user, "", "", "", "", [] as Map, "", ""))
        then:
        thrown IllegalArgumentException
        true
    }

    def "When action is not confirm do not perform any actions"() {
        given:
        def action = new Action("unknown", "command", "type")
        when:
        cut.parse(new SlackInteractiveDto([action], CallbacksId.ConfirmAdd.const, team, channel, user, "", "", "", "", [] as Map, "", ""))
        then:
        0 * _
    }

    def "When adding new valid payment both MoneyService and ChatService should be notified about it"() {
        given:
        def command = "command"
        def action = new Action("confirm", command, "type")
        def to = "to"
        def value = 10
        def payments = [new AddSingleArgument([to], value, [] as Set)]
        def desc = "desc"
        argumentParserService.parse(_, command) >> new AddArgument(payments, desc)
        slackMoneyMerger.mergeUserIntoSequence(payments, user.id, desc) >> CollectionsKt.asSequence([new AddDto(user.id, to, value, desc)])
        when:
        cut.parse(new SlackInteractiveDto([action], CallbacksId.ConfirmAdd.const, team, channel, user, "", "", "", "", [] as Map, "", ""))
        then:
        1 * moneyService.addMoney(new AddDto(user.id, to, value, desc))
        1 * chatService.sendMessage(to, _)
    }

}
