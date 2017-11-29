package pl.org.pablo.slack.money.slack

import pl.org.pablo.slack.money.graph.MoneyRelationshipRepository
import pl.org.pablo.slack.money.slack.request.SendChatMessage
import pl.org.pablo.slack.money.slack.request.SlackCommandExecutor
import spock.lang.Specification

class SlackSchedulerTests extends Specification {

    def moneyR = Mock(MoneyRelationshipRepository)
    def slackUS = Mock(SlackUserService)
    def slackCE = Mock(SlackCommandExecutor)
    def slackSP = Mock(SlackScheduleProperties)

    def cut = new SlackScheduler(moneyR, slackUS, slackCE, slackSP)

    def "Send notification when there are users that are late with paying"() {
        given:
        moneyR.findNotPayingUsers(_) >> ["U1", "U2"]
        slackUS.getUserChannel(_) >> { args -> args[0] }
        when:
        cut.sendNotificationsAboutOutdatedPayments()
        then:
        1 * slackCE.send({(it as SendChatMessage).parameter.channel == "U1"})
        1 * slackCE.send({(it as SendChatMessage).parameter.channel == "U2"})
    }
}
