package pl.org.pablo.slack.money.slack.interactive

import org.springframework.stereotype.Service
import org.springframework.web.util.HtmlUtils
import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.money.MoneyService
import pl.org.pablo.slack.money.slack.ChatService
import pl.org.pablo.slack.money.slack.SlackMoneyMerger
import pl.org.pablo.slack.money.slack.SlackUserService
import pl.org.pablo.slack.money.slack.interactive.CallbacksId.ConfirmAdd
import pl.org.pablo.slack.money.slack.parser.AddArgumentParser
import pl.org.pablo.slack.money.slack.parser.ArgumentParserService

@Service
class SlackInteractiveMessageServiceImpl(
        private val argumentParserService: ArgumentParserService,
        private val chatService: ChatService,
        private val moneyService: MoneyService,
        private val slackMoneyMerger: SlackMoneyMerger,
        private val slackUserService: SlackUserService
) : SlackInteractiveMessageService {

    override fun parse(slackInteractiveDto: SlackInteractiveDto): String {
        val cId = CallbacksId.from(slackInteractiveDto.callbackId) ?: throw IllegalArgumentException("Unknown callback ${slackInteractiveDto.callbackId}")
        return when (cId) {
            ConfirmAdd -> parseConfirmAdd(slackInteractiveDto)
        }
    }

    private fun parseConfirmAdd(slackInteractiveDto: SlackInteractiveDto): String {
        val action = slackInteractiveDto.actions[0]
        if (action.name != "confirm") {
            return "Cancelled"
        }
        val command = HtmlUtils.htmlUnescape(action.value)
        val arg = argumentParserService.parse(AddArgumentParser(), command)
        slackMoneyMerger.mergeUserIntoSequence(arg.payments, slackInteractiveDto.user.id, arg.description)
                .onEach { moneyService.addMoney(it) }
                .forEach { chatService.sendMessage(it.to, getAddNotificationMessage(it)) }
        return "Payment added"
    }

    private fun getAddNotificationMessage(addDto: AddDto): String =
            "Added new payment from: ${slackUserService.getUserName(addDto.from)} with value: ${addDto.value}"

}