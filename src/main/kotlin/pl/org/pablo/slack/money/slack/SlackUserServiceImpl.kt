package pl.org.pablo.slack.money.slack

import org.springframework.stereotype.Component
import pl.org.pablo.slack.money.slack.request.OpenDirectChat
import pl.org.pablo.slack.money.slack.request.SlackCommandExecutor
import pl.org.pablo.slack.money.slack.request.UserDetails
import java.util.concurrent.ConcurrentHashMap

@Component
class SlackUserServiceImpl(
        private val slackCommandExecutor: SlackCommandExecutor
) : SlackUserService {

    private val userNames = ConcurrentHashMap<String, String>()
    private val rooms = ConcurrentHashMap<String, String>()

    override fun getUserChannel(user: String): String = rooms.computeIfAbsent(user, this::generateChannelId)

    private fun generateChannelId(user: String): String {
        val response = slackCommandExecutor.send(OpenDirectChat(OpenDirectChat.Parameter(user)))
        return response.channel.id
    }

    override fun getUserName(user: String): String = userNames.computeIfAbsent(user, this::generateUserId)

    private fun generateUserId(user: String): String {
        val response = slackCommandExecutor.send(UserDetails(UserDetails.Parameter(user)))
        return response.user.name
    }

}
