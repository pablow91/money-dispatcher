package pl.org.pablo.slack.money.money

interface MoneyService {

    fun getBalance(userName: String): List<BalanceDto>

    fun addMoney(addDto: AddDto)
}

data class BalanceDto(
        val name: String,
        val value: Int
)

data class AddDto(
        val from: String,
        val to: String,
        val value: Int,
        val description: String? = null
)
