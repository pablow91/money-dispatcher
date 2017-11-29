package pl.org.pablo.slack.money.test

import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.money.MoneyService
import pl.org.pablo.slack.money.user.UserService

@RestController
@RequestMapping("/test/")
@Profile("dev")
class TestController(
        private val moneyService: MoneyService,
        private val userService: UserService
) {
    @PostMapping("/add/", consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getRequest(@RequestBody addDto: AddDto) {
        moneyService.addMoney(addDto)
    }

    @GetMapping("/balance/{user}/")
    fun getBalance(@PathVariable("user") user: String): String = moneyService.getBalance(user).toString()

    @GetMapping("/history/{user}")
    fun getPaymentHistory(@PathVariable("user") userName: String): String {
        val user = userService.getOrCreate(userName)
        return (user.payed + user.received).asSequence()
                .sortedBy { it.creationDate }
                .joinToString { "${it.payer.name} -> ${it.receiver.name} - ${it.description} - ${it.value} - ${it.creationDate}" }
    }


    @DeleteMapping
    fun clearAll() {
        userService.cleanAll()
    }

}