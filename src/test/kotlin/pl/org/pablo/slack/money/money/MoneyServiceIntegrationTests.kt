package pl.org.pablo.slack.money.money

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [MoneyApplication::class])
class MoneyServiceIntegrationTests {

    @Autowired
    lateinit var cut: MoneyService

    @Autowired
    lateinit var userService: UserService

    @AfterEach
    fun cleanDb() {
        userService.cleanAll()
    }

    //Test utils
    val u1 = UserEntity("u1")
    val u2 = UserEntity("u2")
    val u3 = UserEntity("u3")

    fun pay(from: UserEntity, to: UserEntity, value: Int): PayRelationship {
        cut.addMoney(AddDto(from.name, to.name, value))
        return PayRelationship(from, to, value)
    }

    fun <T> checkList(a: List<T>, b: List<T>, comp: (T, T) -> Boolean) {
        assertEquals(a.size, b.size)
        assertTrue(a.all { q -> b.any { p -> comp(q, p) } })
    }

    fun checkPayRelationship(p1: PayRelationship, p2: PayRelationship): Boolean =
            (p1.payer.name == p2.payer.name) && (p1.receiver.name == p2.receiver.name) && (p1.value == p2.value)

    fun checkBalanceRelationship(b1: BalanceRelationship, b2: BalanceRelationship): Boolean =
            (b1.payer.name == b2.payer.name) && (b1.receiver.name == b2.receiver.name) && (b1.value == b2.value)

    fun check(userEntity: UserEntity) {
        val result = userService.getOrCreate(userEntity.name)
        checkList(userEntity.payed, result.payed, this::checkPayRelationship)
        checkList(userEntity.received, result.received, this::checkPayRelationship)
        checkList(userEntity.toPay, result.toPay, this::checkBalanceRelationship)
        checkList(userEntity.toReturn, result.toReturn, this::checkBalanceRelationship)
    }

    //Tests
    @Test
    fun test2() {
        val p1 = pay(u1, u2, 10)
        val p2 = pay(u2, u3, 10)

        val b1 = BalanceRelationship(u3, u1, 10)

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

    @Test
    fun test3() {
        val p1 = pay(u1, u2, 10)
        val p2 = pay(u2, u3, 5)

        val b1 = BalanceRelationship(u2, u1, 5)
        val b2 = BalanceRelationship(u3, u1, 5)

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

    @Test
    fun test4() {
        val p1 = pay(u1, u2, 10)
        val p2 = pay(u1, u2, 10)

        val b1 = BalanceRelationship(u2, u1, 20)

        check(u1.copy(
                payed = arrayListOf(p1, p2),
                toReturn = arrayListOf(b1)
        ))

        check(u2.copy(
                received = arrayListOf(p1, p2),
                toPay = arrayListOf(b1)
        ))
    }

    @Test
    fun test6() {
        val p1 = pay(u1, u2, 10)
        val p2 = pay(u2, u1, 10)

        check(u1.copy(
                payed = arrayListOf(p1),
                received = arrayListOf(p2)
        ))

        check(u2.copy(
                payed = arrayListOf(p2),
                received = arrayListOf(p1)
        ))
    }

    @Test
    fun test7() {
        val p1 = pay(u1, u2, 10)
        val p2 = pay(u2, u1, 5)

        val b1 = BalanceRelationship(u2, u1, 5)

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

    @Test
    fun test9() {
        val p1 = pay(u1, u2, 10)
        val p2 = pay(u1, u3, 10)
        val p3 = pay(u2, u3, 10)

        val b1 = BalanceRelationship(u3, u1, 20)

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

    @Test
    fun test10() {
        val p1 = pay(u1, u2, 10)
        val p2 = pay(u1, u3, 10)
        val p3 = pay(u2, u3, 20)

        val b1 = BalanceRelationship(u3, u2, 10)
        val b2 = BalanceRelationship(u3, u1, 20)

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

    @Test
    fun test11() {
        val p1 = pay(u1, u2, 10)
        val p2 = pay(u1, u3, 10)
        val p3 = pay(u2, u3, 5)

        val b1 = BalanceRelationship(u2, u1, 5)
        val b2 = BalanceRelationship(u3, u1, 15)

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

}