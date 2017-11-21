package pl.org.pablo.slack.money.graph

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime.now

@ExtendWith(SpringExtension::class)
@DataNeo4jTest
class NotPayingUserTests {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var moneyRelRepository: MoneyRelationshipRepository

    fun String.toUser(): UserEntity = userRepository.save(UserEntity(this))

    fun pay(u1: UserEntity, u2: UserEntity, value: Int): PayRelationship =
            moneyRelRepository.save(PayRelationship(u1, u2, value))

    fun balance(u1: UserEntity, u2: UserEntity, value: Int): BalanceRelationship =
            moneyRelRepository.save(BalanceRelationship(u1, u2, value))

    @DisplayName("Reminder date has been reached - send notification")
    @Test
    fun test1() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()
        pay(u1, u2, 20)
        balance(u2, u1, 20)
        val line = now()

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertEquals(1, result.size)
        assertEquals("u2", result[0])
    }

    @DisplayName("The reminder time is not reached yet - no notification")
    @Test
    fun test2() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()

        val line = now()
        pay(u1, u2, 20)
        balance(u2, u1, 20)

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertTrue(result.isEmpty())
    }

    @DisplayName("Balance is old enough, but there is payment done to other user before remind limit - no notification")
    @Test
    fun test3() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()
        val u3 = "u3".toUser()
        pay(u1, u2, 20)
        balance(u2, u1, 20)
        val line = now()
        pay(u1, u3, 10)
        balance(u3, u1, 10)

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertEquals(1, result.size)
        assertEquals("u2", result[0])
    }

    @DisplayName("Balance is old enough, payment to other user is old enough - send reminder")
    @Test
    fun test4() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()
        val u3 = "u3".toUser()
        pay(u1, u3, 20)
        pay(u2, u1, 20)
        balance(u3, u2, 20)
        val line = now()

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertEquals(1, result.size)
        assertEquals("u3", result[0])
    }

    @DisplayName("One balance is old enough, but second is not - send notification")
    @Test
    fun test5() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()
        val u3 = "u3".toUser()
        pay(u2, u1, 20)
        balance(u1, u2, 20)
        val line = now()
        pay(u3, u1, 20)
        balance(u1, u3, 20)

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertEquals(1, result.size)
        assertEquals("u1", result[0])
    }

    @DisplayName("One user have two balances old enough - send one notification")
    @Test
    fun test6() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()
        val u3 = "u3".toUser()
        pay(u2, u1, 20)
        pay(u3, u1, 20)
        balance(u1, u2, 20)
        balance(u1, u3, 20)
        val line = now()

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertEquals(1, result.size)
        assertEquals("u1", result[0])
    }

    @DisplayName("Two users have balances old enough - send two notification")
    @Test
    fun test7() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()
        val u3 = "u3".toUser()
        val u4 = "u4".toUser()
        pay(u2, u1, 20)
        pay(u4, u3, 20)
        balance(u1, u2, 20)
        balance(u3, u4, 20)
        val line = now()

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertEquals(2, result.size)
        assertEquals(hashSetOf("u1", "u3"), result.toSet())
    }

    @DisplayName("User had positive balance on last payment, balance become negative after line - no notification")
    @Test
    fun test8() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()
        pay(u1, u2, 20)
        pay(u2, u1, 20)
        val line = now()
        pay(u2, u1, 20)
        balance(u1, u2, 20)

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertTrue(result.isEmpty())
    }

    @DisplayName("User had 0 balance on last payment, balance become negative after line - no notification")
    @Test
    fun test9() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()
        pay(u2, u1, 20)
        pay(u1, u2, 20)
        val line = now()
        pay(u2, u1, 20)
        balance(u1, u2, 20)

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertTrue(result.isEmpty())
    }

    @DisplayName("User had 0 balance on last payment, balance become negative before line - send notification")
    @Test
    fun test10() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()
        pay(u2, u1, 20)
        pay(u1, u2, 20)
        pay(u2, u1, 20)
        balance(u1, u2, 20)
        val line = now()

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertTrue(result.isEmpty())
    }

    @DisplayName("User had negative balance on last payment, last payment before line - send notification")
    @Test
    fun test11() {
        val u1 = "u1".toUser()
        val u2 = "u2".toUser()
        pay(u1, u2, 20)
        pay(u2, u1, 10)
        balance(u2, u1, 10)
        val line = now()

        val result = moneyRelRepository.findNotPayingUsers(line.toString())
        assertEquals(1, result.size)
        assertEquals("u2", result[0])
    }

}