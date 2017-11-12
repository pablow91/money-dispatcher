package pl.org.pablo.slack.money.slack

import org.springframework.http.HttpStatus
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/slack/")
class SlackRestController(
        private val slackService: SlackService
) {

    private fun generateRequest(param: MultiValueMap<String, String>): SlackRequest {
        val slackRequest = SlackRequest(param.toSingleValueMap())
        if (slackRequest.token != "d4d24HG6zqjv5rIJVUA5gA0y") {
            throw IllegalAccessException()
        }
        return slackRequest
    }

    @PostMapping("/add/", consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
    fun getRequest(@RequestBody param: MultiValueMap<String, String>): String {
        val slackRequest = generateRequest(param)
        return slackService.add(slackRequest)
    }

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

}
