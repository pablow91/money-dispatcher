package pl.org.pablo.slack.money.slack.request

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "slack.bot")
class SlackProperties {
    lateinit var token: String
    lateinit var base: String
    lateinit var verification: String
}
