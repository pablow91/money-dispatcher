package pl.org.pablo.slack.money.money

import java.math.BigDecimal

/**
 * Deals with managing the state of debt between users.
 */
interface MoneyService {

    /**
     * Get the current state of the users summed debt for every other user.
     *
     * @param userName the userName of the user for which the debt needs to be calculated for
     */
    fun getBalance(userName: String): List<BalanceDto>

    /**
     * Add a debt relation between the user given in the from field to the user given in the to field.
     *
     * @param addDto the dto containing data necessary to create the relation
     */
    fun addMoney(addDto: AddDto)
}

data class BalanceDto(
        val name: String,
        val value: BigDecimal
)

data class AddDto(
        val from: String,
        val to: String,
        val value: BigDecimal,
        val description: String? = null
)

class SelfMoneyException : RuntimeException()