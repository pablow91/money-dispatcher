package pl.org.pablo.slack.money.slack

interface SlackUserService {
    fun getUserChannel(user: String): String

    fun getUserName(user: String): String
}
