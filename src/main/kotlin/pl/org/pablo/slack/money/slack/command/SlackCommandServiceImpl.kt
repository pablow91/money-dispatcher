package pl.org.pablo.slack.money.slack.command

import org.springframework.stereotype.Component
import pl.org.pablo.slack.money.graph.PayRelationship
import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.money.BalanceDto
import pl.org.pablo.slack.money.money.MoneyService
import pl.org.pablo.slack.money.slack.*
import pl.org.pablo.slack.money.slack.interactive.CallbacksId
import pl.org.pablo.slack.money.slack.parser.AddArgumentParser
import pl.org.pablo.slack.money.slack.parser.ArgumentParserService
import pl.org.pablo.slack.money.user.UserService
import java.time.format.DateTimeFormatter

@Component
class SlackCommandServiceImpl(
        private val moneyService: MoneyService,
        private val userService: UserService,
        private val slackUserService: SlackUserService,
        private val argumentParserService: ArgumentParserService,
        private val slackMoneyMerger: SlackMoneyMerger
) : SlackCommandService {

    private val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yy hh:mm")

    override fun add(slackRequest: SlackRequest): InteractiveMessage {
        val arg = argumentParserService.parse(AddArgumentParser(), slackRequest.text)
        val fields = slackMoneyMerger.mergeUserIntoSequence(arg.payments, slackRequest.user_id, arg.description)
                .sortedBy { it.to }
                .map { it.toField() }
                .toList()
        return InteractiveMessage(
                text = "Would you like to config the payment?",
                attachments = listOf(
                        Attachment(
                                fallback = "You are unable to confirm the payment",
                                callbackId = CallbacksId.ConfirmAdd.const,
                                attachmentType = "default",
                                fields = fields,
                                actions = listOf(
                                        Action(
                                                name = "confirm",
                                                text = "Confirm",
                                                type = MessageActionType.BUTTON,
                                                style = Style.DEFAULT,
                                                value = slackRequest.text
                                        ),
                                        Action(
                                                name = "decline",
                                                text = "Decline",
                                                type = MessageActionType.BUTTON,
                                                style = Style.DEFAULT
                                        )
                                )
                        )
                )
        )
    }

    private fun AddDto.toField(): Field = Field(
            title = slackUserService.getUserName(to),
            value = value.toString(),
            short = true
    )

    override fun getBalance(slackRequest: SlackRequest): InteractiveMessage {
        val userName = slackRequest.user_id
        val balance = moneyService.getBalance(userName)
        if (balance.isEmpty()) {
            return InteractiveMessage("All good my dear friend!")
        }
        val fields = balance.map { it.toField() }
        val fallbackString = balance.joinToString(separator = "\n") { "${slackUserService.getUserName(it.name)} ${it.value}" }

        return InteractiveMessage(
                text = "Your current balance",
                attachments = listOf(
                        Attachment(
                                fallback = fallbackString,
                                callbackId = "none",
                                fields = fields,
                                attachmentType = "default",
                                actions = listOf()
                        )
                )
        )
    }

    fun BalanceDto.toField(): Field = Field(
            title = slackUserService.getUserName(this.name),
            value = this.value.toString(),
            short = true
    )

    override fun getPaymentHistory(slackRequest: SlackRequest): InteractiveMessage {
        val userName = slackRequest.user_id
        val user = userService.getOrCreate(userName)
        val history = (user.payed + user.received).sortedBy { it.date }

        val fields = history.map { it.toField() }
        val fallbackString = history.joinToString(separator = "\n") { "${slackUserService.getUserName(it.payer.name)} -> ${slackUserService.getUserName(it.receiver.name)} - ${it.description} - ${it.value} - ${it.date.format(dateFormat)}" }

        return InteractiveMessage(
                text = "Your payment history",
                attachments = listOf(
                        Attachment(
                                fallback = fallbackString,
                                callbackId = "none",
                                fields = fields,
                                attachmentType = "default",
                                actions = listOf()
                        )
                )
        )
    }

    fun PayRelationship.toField(): Field = Field(
            title = "${slackUserService.getUserName(payer.name)} -> ${slackUserService.getUserName(receiver.name)}",
            value = "$value - ${date.format(dateFormat)} ${description ?: ""}",
            short = false
    )

}
