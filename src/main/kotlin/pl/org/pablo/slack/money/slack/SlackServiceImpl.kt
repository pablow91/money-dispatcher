package pl.org.pablo.slack.money.slack

import org.springframework.stereotype.Component
import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.money.MoneyService
import pl.org.pablo.slack.money.user.UserService
import java.time.format.DateTimeFormatter

@Component
class SlackServiceImpl(
        private val moneyService: MoneyService,
        private val userService: UserService,
        private val chatService: ChatService,
        private val slackUserService: SlackUserService
) : SlackService {

    private val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yy hh:mm")

    override fun add(slackRequest: SlackRequest): String {
        val text = slackRequest.text.replace(Regex("<@([aA-zZ0-9]+)(\\|[aA-zZ0-9]+)?>"), { "@${it.destructured.component1()}" })
        val regex = Regex("@([aA-zZ0-9]+) ([0-9]+) ?(.*)?")
        if (!text.matches(regex)) {
            throw IllegalArgumentException("Wrong command $text")
        }
        val (_, user, value, description) = regex.find(text)!!.groupValues
        moneyService.addMoney(AddDto(slackRequest.user_id, user, value.toInt(), description))
        chatService.sendMessage(user, "Added new payment from ${slackRequest.user_name} with value: $value")
        return "Success"
    }

    override fun getBalance(slackRequest: SlackRequest): String {
        val userName = slackRequest.user_id
        val balance = moneyService.getBalance(userName)
        if (balance.isEmpty()) {
            return "All good my dear friend!"
        }
        return balance.joinToString(separator = "\n") { "${slackUserService.getUserName(it.name)} ${it.value}" }
    }

    override fun getPaymentHistory(slackRequest: SlackRequest): String {
        val userName = slackRequest.user_id
        val user = userService.getOrCreate(userName)
        return (user.payed + user.received).asSequence()
                .sortedBy { it.date }
                .joinToString(separator = "\n") { "${slackUserService.getUserName(it.payer.name)} -> ${slackUserService.getUserName(it.receiver.name)} - ${it.description} - ${it.value} - ${it.date.format(dateFormat)}" }
    }

}
