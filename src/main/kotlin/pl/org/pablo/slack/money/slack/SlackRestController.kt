package pl.org.pablo.slack.money.slack

import org.springframework.http.HttpStatus
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import pl.org.pablo.slack.money.money.SelfMoneyException
import pl.org.pablo.slack.money.slack.request.SlackProperties

@RestController
@RequestMapping("/slack/")
class SlackRestController(
        private val slackService: SlackService,
        private val slackProperties: SlackProperties
) {

    private fun generateRequest(param: MultiValueMap<String, String>): SlackRequest {
        val slackRequest = SlackRequest(param.toSingleValueMap())
        if (slackRequest.token != slackProperties.verification) {
            throw IllegalAccessException()
        }
        return slackRequest
    }

    @PostMapping("/add/", consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
    fun addRequest(@RequestBody param: MultiValueMap<String, String>): String {
        val slackRequest = generateRequest(param)
        return slackService.add(slackRequest)
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(SelfMoneyException::class)
    fun handleSelfMoneyException() = "You cannot add payment to yourself"

    @PostMapping("/balance/", consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
    fun getBalance(@RequestBody param: MultiValueMap<String, String>): String {
        val slackRequest = generateRequest(param)
        return slackService.getBalance(slackRequest)
    }

    @PostMapping("/history/", consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
    fun getPaymentHistory(@RequestBody param: MultiValueMap<String, String>): String {
        val slackRequest = generateRequest(param)
        return slackService.getPaymentHistory(slackRequest)
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Bad token")
    @ExceptionHandler(IllegalAccessException::class)
    fun handle() {
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument() = "Invalid command"

}
