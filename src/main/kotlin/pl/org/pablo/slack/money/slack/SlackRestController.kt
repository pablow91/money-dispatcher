package pl.org.pablo.slack.money.slack

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import pl.org.pablo.slack.money.money.SelfMoneyException
import pl.org.pablo.slack.money.slack.command.SlackCommandService
import pl.org.pablo.slack.money.slack.command.SlackRequest
import pl.org.pablo.slack.money.slack.interactive.SlackInteractiveDto
import pl.org.pablo.slack.money.slack.interactive.SlackInteractiveMessageService
import pl.org.pablo.slack.money.slack.request.SlackProperties

@RestController
@RequestMapping("/slack/")
class SlackRestController(
        private val slackCommandService: SlackCommandService,
        private val slackInteractiveMessageService: SlackInteractiveMessageService,
        private val slackProperties: SlackProperties,
        private val objectMapper: ObjectMapper
) {

    private fun generateRequest(param: MultiValueMap<String, String>): SlackRequest {
        val slackRequest = SlackRequest(param.toSingleValueMap())
        verifyToken(slackRequest.token)
        return slackRequest
    }

    private fun verifyToken(token: String) {
        if (token != slackProperties.verification) {
            throw IllegalAccessException()
        }
    }

    @PostMapping("/interactive/", consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
    fun addConfirmedRequest(@RequestBody param: MultiValueMap<String, String>): String {
        val payload = param.getFirst("payload") ?: throw IllegalArgumentException("Payload not provided")
        val msg = objectMapper.readValue(payload, SlackInteractiveDto::class.java)
        return slackInteractiveMessageService.parse(msg)
    }

    @PostMapping("/add/", consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
    fun addRequest(@RequestBody param: MultiValueMap<String, String>): InteractiveMessage {
        val slackRequest = generateRequest(param)
        return slackCommandService.add(slackRequest)
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(SelfMoneyException::class)
    fun handleSelfMoneyException() = "You cannot add payment to yourself"

    @PostMapping("/balance/", consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
    fun getBalance(@RequestBody param: MultiValueMap<String, String>): InteractiveMessage {
        val slackRequest = generateRequest(param)
        return slackCommandService.getBalance(slackRequest)
    }

    @PostMapping("/history/", consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
    fun getPaymentHistory(@RequestBody param: MultiValueMap<String, String>): InteractiveMessage {
        val slackRequest = generateRequest(param)
        return slackCommandService.getPaymentHistory(slackRequest)
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Bad token")
    @ExceptionHandler(IllegalAccessException::class)
    fun handle(ex: IllegalAccessException) {
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): String {
        return "Invalid command"
    }

}
