package pl.org.pablo.slack.money.slack

import org.springframework.stereotype.Component
import pl.org.pablo.slack.money.slack.request.SendChatMessage
import pl.org.pablo.slack.money.slack.request.SlackCommandExecutor

@Component
class ChatServiceImpl(
        private val slackUserService: SlackUserService,
        private val slackCommandExecutor: SlackCommandExecutor
) : ChatService {

    override fun sendMessage(user: String, message: String) {
        val userChannel = slackUserService.getUserChannel(user)
        val msg = SendChatMessage(SendChatMessage.Parameter(userChannel, message))
        slackCommandExecutor.send(msg)
    }

}
