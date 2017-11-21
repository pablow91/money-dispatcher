package pl.org.pablo.slack.money.slack

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "slack.bot")
class SlackScheduleProperties {
    lateinit var cron: String
    var remissionDays: Long = 0
}