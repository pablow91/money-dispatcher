package pl.org.pablo.slack.money.slack.request

import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.nio.charset.Charset

@Component
class SlackCommandExecutorImpl(
        private val slackProperties: SlackProperties
) : SlackCommandExecutor {
    private val restTemplate = RestTemplate()

    override fun <P : SlackParameter, R : Any> send(command: SlackCommand<P, R>): R {
        val address = makeAddress(command)
        val parameter = command.parameter
        val resultType = command.resultType

        val headers = HttpHeaders()
        headers.contentType = MediaType(MediaType.APPLICATION_FORM_URLENCODED, Charset.forName("utf8"))
        headers.add("Authorization", "Bearer ${slackProperties.token}")
        val params = LinkedMultiValueMap<String, String>()
                .apply { parameter.multiValueMap().invoke(this) }
        val httpEntity = HttpEntity(params, headers)

        val response: ResponseEntity<R> = restTemplate.exchange(address, HttpMethod.POST, httpEntity, resultType)

        if (response.statusCode != HttpStatus.OK || !response.hasBody()) {
            throw IllegalStateException("Slack response is bad")
        }
        return response.body!!
    }

    private fun <P : SlackParameter, R> makeAddress(command: SlackCommand<P, R>): String = "${slackProperties.base}${command.address}"

}
