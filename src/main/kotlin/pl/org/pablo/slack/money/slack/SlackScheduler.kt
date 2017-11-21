package pl.org.pablo.slack.money.slack

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.org.pablo.slack.money.graph.MoneyRelationshipRepository
import pl.org.pablo.slack.money.slack.request.SendChatMessage
import pl.org.pablo.slack.money.slack.request.SlackCommandExecutor
import java.time.LocalDateTime

@Component
class SlackScheduler(
        private val moneyRelationshipRepository: MoneyRelationshipRepository,
        private val slackUserService: SlackUserService,
        private val slackCommandExecutor: SlackCommandExecutor,
        private val slackScheduleProperties: SlackScheduleProperties
) {

    @Scheduled(cron = "\${slack.notification.cron}")
    fun sendNotificationsAboutOutdatedPayments() {
        val barrierDate = LocalDateTime.now().minusDays(slackScheduleProperties.remissionDays)
        val users = moneyRelationshipRepository.findNotPayingUsers(barrierDate.toString())
        users.forEach(this::sendNotification)
    }

    private fun sendNotification(userName: String) {
        val channel = slackUserService.getUserChannel(userName)
        slackCommandExecutor.send(SendChatMessage(SendChatMessage.Parameter(channel, "You are late with payments. Consider making one :)")))
    }
}