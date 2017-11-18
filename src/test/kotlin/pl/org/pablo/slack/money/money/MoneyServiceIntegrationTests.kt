package pl.org.pablo.slack.money.money

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import pl.org.pablo.slack.money.MoneyApplication
import pl.org.pablo.slack.money.graph.BalanceRelationship
import pl.org.pablo.slack.money.graph.PayRelationship
import pl.org.pablo.slack.money.graph.UserEntity
import pl.org.pablo.slack.money.user.UserService
import java.math.BigDecimal

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [MoneyApplication::class])
class MoneyServiceIntegrationTests {

    @Autowired
    lateinit var cut: MoneyService

    @Autowired
    lateinit var userService: UserService

    @BeforeEach
    @AfterEach
    fun cleanDb() {
        userService.cleanAll()
    }

    //Test data
    val u1 = UserEntity("u1")
    val u2 = UserEntity("u2")
    val u3 = UserEntity("u3")

    val five = BigDecimal(5)
    val ten = BigDecimal.TEN!!
    val fifteen = BigDecimal(15)
    val twenty = BigDecimal(20)

    //Tests
    @DisplayName("U1 pays to U2 and U2 pays to U3 same amounts -> U3 has to give all to U1")
    @Test
    fun test1() {
        val p1 = pay(u1, u2, ten)
        val p2 = pay(u2, u3, ten)

        val b1 = BalanceRelationship(u3, u1, ten)

        check(u1.copy(
                payed = arrayListOf(p1),
                toReturn = arrayListOf(b1)
        ))

        check(u2.copy(
                payed = arrayListOf(p2),
                received = arrayListOf(p1)
        ))

        check(u3.copy(
                received = arrayListOf(p2),
                toPay = arrayListOf(b1)
        ))
    }

    @DisplayName("U1 pays to U2 and U2 pays half to U3 -> U3 give 5 to U1 and 5 to U2")
    @Test
    fun test2() {
        val p1 = pay(u1, u2, ten)
        val p2 = pay(u2, u3, five)

        val b1 = BalanceRelationship(u2, u1, five)
        val b2 = BalanceRelationship(u3, u1, five)

        check(u1.copy(
                payed = arrayListOf(p1),
                toReturn = arrayListOf(b1, b2)
        ))

        check(u2.copy(
                payed = arrayListOf(p2),
                received = arrayListOf(p1),
                toPay = arrayListOf(b1)
        ))

        check(u3.copy(
                received = arrayListOf(p2),
                toPay = arrayListOf(b2)
        ))
    }

    @DisplayName("U1 pays U2 twice -> U2 has to pay all to U1")
    @Test
    fun test3() {
        val p1 = pay(u1, u2, ten)
        val p2 = pay(u1, u2, ten)

        val b1 = BalanceRelationship(u2, u1, twenty)

        check(u1.copy(
                payed = arrayListOf(p1, p2),
                toReturn = arrayListOf(b1)
        ))

        check(u2.copy(
                received = arrayListOf(p1, p2),
                toPay = arrayListOf(b1)
        ))
    }

    @DisplayName("U1 pays U2 same amount as U2 pays to U1 -> nobody pays anything")
    @Test
    fun test4() {
        val p1 = pay(u1, u2, ten)
        val p2 = pay(u2, u1, ten)

        check(u1.copy(
                payed = arrayListOf(p1),
                received = arrayListOf(p2)
        ))

        check(u2.copy(
                payed = arrayListOf(p2),
                received = arrayListOf(p1)
        ))
    }

    @DisplayName("U1 pays U2 and U2 pays half to U1 -> U2 has to pay U1 other half")
    @Test
    fun test5() {
        val p1 = pay(u1, u2, ten)
        val p2 = pay(u2, u1, five)

        val b1 = BalanceRelationship(u2, u1, five)

        check(u1.copy(
                payed = arrayListOf(p1),
                received = arrayListOf(p2),
                toReturn = arrayListOf(b1)
        ))

        check(u2.copy(
                payed = arrayListOf(p2),
                received = arrayListOf(p1),
                toPay = arrayListOf(b1)
        ))
    }

    @DisplayName("U1 pays U2 and U3 same amount and U2 pays U3 -> U3 has to pay all to U1")
    @Test
    fun test6() {
        val p1 = pay(u1, u2, ten)
        val p3 = pay(u2, u3, ten)
        val p2 = pay(u1, u3, ten)

        val b1 = BalanceRelationship(u3, u1, twenty)

        check(u1.copy(
                payed = arrayListOf(p1, p2),
                toReturn = arrayListOf(b1)
        ))

        check(u2.copy(
                payed = arrayListOf(p3),
                received = arrayListOf(p1)
        ))

        check(u3.copy(
                received = arrayListOf(p2, p3),
                toPay = arrayListOf(b1)
        ))
    }

    @DisplayName("U1 pays U2 and U3 same amount and U2 pays U3 twice as much -> U3 pays to U1 and U2")
    @Test
    fun test7() {
        val p1 = pay(u1, u2, ten)
        val p2 = pay(u1, u3, ten)
        val p3 = pay(u2, u3, twenty)

        val b1 = BalanceRelationship(u3, u2, ten)
        val b2 = BalanceRelationship(u3, u1, twenty)

        check(u1.copy(
                payed = arrayListOf(p1, p2),
                toReturn = arrayListOf(b2)
        ))

        check(u2.copy(
                payed = arrayListOf(p3),
                received = arrayListOf(p1),
                toReturn = arrayListOf(b1)
        ))

        check(u3.copy(
                received = arrayListOf(p2, p3),
                toPay = arrayListOf(b1, b2)
        ))
    }

    @DisplayName("U1 pays U2 and U3 same amount and U2 pays U3 half -> U3 pays to U1 and U2 pays to U1")
    @Test
    fun test8() {
        val p1 = pay(u1, u2, ten)
        val p2 = pay(u1, u3, ten)
        val p3 = pay(u2, u3, five)

        val b1 = BalanceRelationship(u2, u1, five)
        val b2 = BalanceRelationship(u3, u1, fifteen)

        check(u1.copy(
                payed = arrayListOf(p1, p2),
                toReturn = arrayListOf(b1, b2)
        ))

        check(u2.copy(
                payed = arrayListOf(p3),
                received = arrayListOf(p1),
                toPay = arrayListOf(b1)
        ))

        check(u3.copy(
                received = arrayListOf(p2, p3),
                toPay = arrayListOf(b2)
        ))
    }

    fun pay(from: UserEntity, to: UserEntity, value: BigDecimal): PayRelationship {
        cut.addMoney(AddDto(from.name, to.name, value))
        return PayRelationship(from, to, value)
    }

    fun <T> checkList(a: List<T>, b: List<T>, comp: (T, T) -> Boolean) {
        assertEquals(a.size, b.size)
        assertTrue(a.all { q -> b.any { p -> comp(q, p) } })
    }

    fun checkPayRelationship(p1: PayRelationship, p2: PayRelationship): Boolean =
            (p1.payer.name == p2.payer.name) && (p1.receiver.name == p2.receiver.name) && (p1.value.compareTo(p2.value) == 0)

    fun checkBalanceRelationship(b1: BalanceRelationship, b2: BalanceRelationship): Boolean =
            (b1.payer.name == b2.payer.name) && (b1.receiver.name == b2.receiver.name) && (b1.value.compareTo(b2.value) == 0)

    fun check(userEntity: UserEntity) {
        val result = userService.getOrCreate(userEntity.name)
        checkList(userEntity.payed, result.payed, this::checkPayRelationship)
        checkList(userEntity.received, result.received, this::checkPayRelationship)
        checkList(userEntity.toPay, result.toPay, this::checkBalanceRelationship)
        checkList(userEntity.toReturn, result.toReturn, this::checkBalanceRelationship)
    }

}