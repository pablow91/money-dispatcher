package pl.org.pablo.slack.money.money

import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import pl.org.pablo.slack.money.graph.BalanceRelationship
import pl.org.pablo.slack.money.graph.MoneyRelationshipRepository
import pl.org.pablo.slack.money.graph.PayRelationship
import pl.org.pablo.slack.money.graph.UserEntity
import pl.org.pablo.slack.money.user.UserService

@Controller
class MoneyServiceImpl(
        private val userService: UserService,
        private val moneyRelationshipRepository: MoneyRelationshipRepository
) : MoneyService {

    override fun getBalance(userName: String): List<BalanceDto> =
            userService.getOrCreate(userName).getBalance()

    @Transactional
    override fun addMoney(addDto: AddDto) {
        if (addDto.from == addDto.to) {
            throw SelfMoneyException()
        }
        // Find who owes to whom
        val u1 = userService.getOrCreate(addDto.from)
        val u2 = userService.getOrCreate(addDto.to)
        val swap = addDto.value < 0

        val from = if (swap) u2 else u1
        val to = if (swap) u1 else u2
        val value = if (swap) -addDto.value else addDto.value
        val relation = PayRelationship(from, to, value, addDto.description)
        moneyRelationshipRepository.save(relation, 0)
        updateBalance(from, to, value)
    }

    /**
     * Update the state of the debt between the to given users by the given value.
     *
     * @param from the user which owes money
     * @param to the user which is owed to
     * @param value the amount of money which is owed
     */
    private fun updateBalance(from: UserEntity, to: UserEntity, value: Int) {
        var remainingValue = reduceDebt(from, to, value)
        if (remainingValue == 0) {
            return
        }
        // Check if a user owes anyone money and then move this debt
        if (from.toPay.isNotEmpty()) {
            val res = from.toPay.asSequence().sortedBy { it.value }
                    .groupBy { to.toPay.any {o -> it.receiver.id == o.receiver.id} }
            res[true]?.forEach {
                val opValue: Int
                val diff: Int
                if (it.value <= remainingValue) {
                    from.toPay.remove(it)
                    to.toReturn.remove(it)
                    moneyRelationshipRepository.delete(it)
                    opValue = it.value
                    diff = remainingValue - it.value
                } else {
                    opValue = remainingValue
                    it.value -= remainingValue
                    diff = 0
                    moneyRelationshipRepository.save(it, 0)
                }
                remainingValue = reduceDebt(it.receiver, to, opValue) + diff
                if (remainingValue == 0) {
                    return
                }
            }
            res[false]?.forEach {
                remainingValue = transferDebt(to, it, remainingValue)
                if (remainingValue == 0) {
                    return
                }
            }
        }
        // Create debt between users
        val newDebt = BalanceRelationship(to, from, remainingValue)
        moneyRelationshipRepository.save(newDebt, 0)
    }

    /**
     * Reduce the debt between two users according to the given value.
     *
     * @param from the user from which the debt comes from
     * @param to the user to which the debt goes to
     * @param remainingValue the amount by which the debt has to be changed
     */
    private fun reduceDebt(from: UserEntity, to: UserEntity, remainingValue: Int): Int {
        // Check if I owe him money
        val node = from.toPay.find { it.receiver.id == to.id }
        if (node != null) {
            return if (node.value > remainingValue) {
                // Reduce the debt
                node.value -= remainingValue
                moneyRelationshipRepository.save(node, 0)
                0
            } else {
                // The debt is paid so remove the connection
                from.toPay.remove(node)
                to.toReturn.remove(node)
                moneyRelationshipRepository.delete(node)
                remainingValue - node.value
            }
        } else {
            // Check if the other user owes me money
            val node2 = from.toReturn.find { it.payer.id == to.id }
            if (node2 != null) {
                node2.value += remainingValue
                moneyRelationshipRepository.save(node2, 0)
                return 0
            }
        }

        // There is no debt relationship between those two nodes
        return remainingValue
    }

    /**
     * Transfer debt between the given user and his other debtors.
     *
     * @param from the user from which a debt is taken
     * @param old the other debtors of this user
     * @param remainingValue the amount of debt to transfer
     */
    private fun transferDebt(from: UserEntity, old: BalanceRelationship, remainingValue: Int): Int {
        return if (remainingValue >= old.value) {
            val bal = BalanceRelationship(from, old.receiver, old.value)
            moneyRelationshipRepository.save(bal, 0)

            old.payer.toPay.remove(old)
            old.receiver.toReturn.remove(old)
            moneyRelationshipRepository.delete(old)

            remainingValue - old.value
        } else {
            old.value -= remainingValue
            val bal = BalanceRelationship(from, old.receiver, remainingValue)
            moneyRelationshipRepository.saveAll(listOf(old, bal))
            0
        }
    }

}

/**
 * Get the current balance of the given user.
 */
private fun UserEntity.getBalance(): List<BalanceDto> =
        toReturn.map { BalanceDto(it.payer.name, it.value) } + toPay.map { BalanceDto(it.receiver.name, -it.value) }