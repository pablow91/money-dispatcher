package pl.org.pablo.slack.money.slack

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty

enum class MessageActionType {
    @JsonProperty("button")
    BUTTON,
    @JsonProperty("select")
    SELECT
}

@JsonInclude(NON_NULL)
data class ConfirmationHash(
        val title: String? = null,
        val text: String,
        @JsonProperty("okText") val okText: String? = null,
        @JsonProperty("dismiss_text") val dismissText: String? = null
)

enum class Style {
    @JsonProperty("default")
    DEFAULT,
    @JsonProperty("primary")
    PRIMARY,
    @JsonProperty("danger")
    DANGER
}

@JsonInclude(NON_NULL)
data class Action(
        val name: String,
        val text: String,
        val type: MessageActionType,
        val value: String? = null,
        val confirm: ConfirmationHash? = null,
        val style: Style
)

data class Field(
        val title: String,
        val value: String,
        val short: Boolean
)

@JsonInclude(NON_NULL)
data class Attachment(
        val title: String? = null,
        val fallback: String,
        @JsonProperty("callback_id") val callbackId: String,
        val color: String? = null,
        val actions: List<Action>,
        val fields: List<Field> = listOf(),
        @JsonProperty("attachment_type") val attachmentType: String
)

enum class ResponseType {
    @JsonProperty("in_channel")
    IN_CHANNEL,
    @JsonProperty("ephemeral")
    EPHEMERAL
}

@JsonInclude(NON_NULL)
data class InteractiveMessage(
        val text: String? = null,
        val attachments: List<Attachment> = listOf(),
        @field:JsonProperty("thread_ts") val threadTs: String? = null,
        @field:JsonProperty("response_type") val responseType: ResponseType? = null,
        @field:JsonProperty("replace_original") val replaceOriginal: Boolean? = null,
        @field:JsonProperty("delete_original") val deleteOriginal: Boolean? = null
) {
    init {
        if (text == null && attachments.isEmpty()) {
            throw IllegalStateException("text or attachments must be not null")
        }
    }
}

