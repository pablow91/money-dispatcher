package pl.org.pablo.slack.money.slack

import org.springframework.stereotype.Component
import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.money.MoneyService
import pl.org.pablo.slack.money.slack.parser.AddArgumentParser
import pl.org.pablo.slack.money.slack.parser.AddSingleArgument
import pl.org.pablo.slack.money.slack.parser.ArgumentParserService
import pl.org.pablo.slack.money.user.UserService
import pl.org.pablo.slack.money.withMoneyScale
import java.time.format.DateTimeFormatter

@Component
class SlackServiceImpl(
        private val moneyService: MoneyService,
        private val userService: UserService,
        private val chatService: ChatService,
        private val slackUserService: SlackUserService,
        private val argumentParserService: ArgumentParserService
) : SlackService {

    private val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yy hh:mm")
    private val addArgumentParser = AddArgumentParser()

    override fun add(slackRequest: SlackRequest): String {
        val arg = argumentParserService.parse(addArgumentParser, slackRequest.text)
        arg.payments.asSequence()
                .flatMap { toAddDto(slackRequest.user_id, it, arg.description) }
                .groupBy { it.to }
                .map { mergeOneUserAddDto(it.value) }
                .onEach { moneyService.addMoney(it) }
                .forEach { chatService.sendMessage(it.to, getAddNotificationMessage(it)) }
        return "Success"
    }

    private fun mergeOneUserAddDto(value: List<AddDto>): AddDto {
        val dto = value.asSequence().reduce { acc, addDto -> acc.copy(value = acc.value + addDto.value) }
        return dto.copy(value = dto.value.withMoneyScale())
    }

    private fun toAddDto(from: String, arg: AddSingleArgument, desc: String?): Sequence<AddDto> {
        var perUserValue = arg.value
        if (arg.options.contains("eq")) {
            perUserValue /= arg.users.size.toBigDecimal().withMoneyScale()
        }
        return arg.users.asSequence().map { AddDto(from, it, perUserValue, desc) }
    }

    private fun getAddNotificationMessage(addDto: AddDto): String =
            "Added new payment from: ${addDto.from} with value: ${addDto.value}"

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
