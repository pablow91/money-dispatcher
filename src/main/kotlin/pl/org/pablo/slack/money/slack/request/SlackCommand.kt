package pl.org.pablo.slack.money.slack.request

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

interface SlackParameter {
    fun multiValueMap(): (LinkedMultiValueMap<String, String>) -> Unit
}

abstract class SlackCommand<out P : SlackParameter, R>(
        val parameter: P
) {
    abstract val address: String
    abstract val resultType: Class<R>
}


class OpenDirectChat(parameter: Parameter)
    : SlackCommand<OpenDirectChat.Parameter, OpenDirectChat.Response>(parameter) {

    override val address: String
        get() = "im.open"
    override val resultType: Class<Response>
        get() = Response::class.java

    data class Parameter(
            val user: String
    ) : SlackParameter {
        override fun multiValueMap(): (MultiValueMap<String, String>) -> Unit = {
            it.add("user", user)
            it.add("return_im", true.toString())
        }
    }

    data class Response(
            val ok: Boolean,
            val channel: Channel
    )

    data class Channel(val id: String)
}

class SendChatMessage(parameter: Parameter)
    : SlackCommand<SendChatMessage.Parameter, SendChatMessage.Response>(parameter) {

    override val address: String
        get() = "chat.postMessage"
    override val resultType: Class<Response>
        get() = Response::class.java

    data class Parameter(
            val channel: String,
            val text: String
    ) : SlackParameter {
        override fun multiValueMap(): (MultiValueMap<String, String>) -> Unit = {
            it.add("channel", channel)
            it.add("text", text)
        }
    }

    data class Response(
            val ok: Boolean
    )

}

class UserDetails(parameter: Parameter)
    : SlackCommand<UserDetails.Parameter, UserDetails.Response>(parameter) {

    override val address: String
        get() = "users.info"
    override val resultType: Class<Response>
        get() = Response::class.java

    data class Parameter(
            val user: String
    ) : SlackParameter {
        override fun multiValueMap(): (MultiValueMap<String, String>) -> Unit = {
            it.add("user", user)
        }
    }

    data class Response(
            val ok: Boolean,
            val user: User
    )

    data class User(
            val name: String
    )

}
