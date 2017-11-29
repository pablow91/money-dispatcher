package pl.org.pablo.slack.money.graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.neo4j.ogm.session.Session
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal

@ExtendWith(SpringExtension::class)
@DataNeo4jTest
class ModelIntegrationTests {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var moneyRelRepository: MoneyRelationshipRepository

    @Autowired
    lateinit var session: Session

    @Test
    fun basicPaymentTestUsingMoneyRepository() {
        val u1 = UserEntity("u1")
        val u2 = UserEntity("u2")

        val rel = PayRelationship(u1, u2, BigDecimal.TEN, "desc")
        moneyRelRepository.save(rel)

        session.clear()

        val result = userRepository.findByName("u1") ?: throw NullPointerException()
        val payRel = result.payed[0]
        assertEquals(u1.name, payRel.payer.name)
        assertEquals(u2.name, payRel.receiver.name)
        assertEquals(BigDecimal.TEN, payRel.value)
        assertNotNull(payRel.creationDate)
        assertNotNull(payRel.description)
    }

    @Test
    fun basicPaymentTestUsingUserRepository() {
        val u1 = UserEntity("u1")
        val u2 = UserEntity("u2")
        userRepository.save(u1)
        userRepository.save(u2)
        session.clear()

        u1.payed.add(PayRelationship(u1, u2, BigDecimal.TEN, "desc"))
        userRepository.save(u1)

        session.clear()
        val result = userRepository.findByName("u1")
        assertEquals(1, result!!.payed.size)
    }

}