package pl.org.pablo.slack.money.slack

interface SlackService {
    fun add(slackRequest: SlackRequest): String

    fun getBalance(slackRequest: SlackRequest): String

    fun getPaymentHistory(slackRequest: SlackRequest): String
}

class SlackRequest(map: Map<String, String>) {
    val token: String by map
    val team_id: String by map
    val team_domain: String by map
    val channel_id: String by map
    val channel_name: String by map
    val user_id: String by map
    val user_name: String by map
    val command: String by map
    val text: String by map
    val response_url: String by map
}
