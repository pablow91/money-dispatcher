package pl.org.pablo.slack.money.slack.interactive

import com.fasterxml.jackson.annotation.JsonProperty

interface SlackInteractiveMessageService {
    /**
     * Parsing slack request that originates from interactive messages
     * @param slackInteractiveDto - https://api.slack.com/docs/interactive-message-field-guide#action_payload
     * @return value that will replace interactive message
     */
    fun parse(slackInteractiveDto: SlackInteractiveDto): String
}

data class Action(
        val name: String,
        val value: String,
        val type: String
)

data class Team(
        val id: String,
        val domain: String
)

data class Channel(
        val id: String,
        val name: String
)

data class User(
        val id: String,
        val name: String
)

data class SlackInteractiveDto(
        val actions: List<Action> = listOf(),
        @JsonProperty("callback_id") val callbackId: String,
        val team: Team,
        val channel: Channel,
        val user: User,
        @JsonProperty("action_ts") val actionTs: String,
        @JsonProperty("message_ts") val messageTs: String,
        @JsonProperty("attachment_id") val attachmentId: String,
        @JsonProperty("token") val token: String,
        @JsonProperty("original_message") val originalMessage: Map<String, Any>?,
        @JsonProperty("response_url") val responseUrl: String,
        @JsonProperty("trigger_id") val triggerId: String
)
