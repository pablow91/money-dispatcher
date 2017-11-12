package pl.org.pablo.slack.money.slack

interface ChatService {
    fun sendMessage(user: String, message: String)
}
