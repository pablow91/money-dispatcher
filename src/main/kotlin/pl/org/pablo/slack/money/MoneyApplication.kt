package pl.org.pablo.slack.money

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class MoneyApplication

fun main(args: Array<String>) {
    SpringApplication.run(MoneyApplication::class.java, *args)
}
