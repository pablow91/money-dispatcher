package pl.org.pablo.slack.money.graph

import org.neo4j.ogm.annotation.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@NodeEntity
data class UserEntity(
        var name: String,

        @Relationship(type = "PAY", direction = Relationship.OUTGOING) var payed: MutableList<PayRelationship> = arrayListOf(),
        @Relationship(type = "PAY", direction = Relationship.INCOMING) var received: MutableList<PayRelationship> = arrayListOf(),

        @Relationship(type = "BALANCE", direction = Relationship.OUTGOING) var toPay: MutableList<BalanceRelationship> = arrayListOf(),
        @Relationship(type = "BALANCE", direction = Relationship.INCOMING) var toReturn: MutableList<BalanceRelationship> = arrayListOf(),

        var id: Long? = null
) {
    protected constructor() : this("")

    companion object {
        val STUB = UserEntity()
    }
}


abstract class MoneyRelationship(
        @StartNode var payer: UserEntity,
        @EndNode var receiver: UserEntity,
        @Property var value: BigDecimal
) {
    var id: Long? = null
    @CreatedDate
    var creationDate: LocalDateTime = LocalDateTime.now()
    @LastModifiedDate
    var modificationDate: LocalDateTime = LocalDateTime.now()
    @Property
    var uuid: String = UUID.randomUUID().toString()

    protected constructor() : this(UserEntity.STUB, UserEntity.STUB, BigDecimal.ZERO)
}

@RelationshipEntity(type = "PAY")
class PayRelationship(payer: UserEntity,
                      receiver: UserEntity,
                      value: BigDecimal,
                      var description: String? = null
) : MoneyRelationship(payer, receiver, value) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PayRelationship

        if (receiver != other.receiver) return false
        if (payer != other.payer) return false
        if (description != other.description) return false
        if (value != other.value) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = description?.hashCode() ?: 0

}

@RelationshipEntity(type = "BALANCE")
class BalanceRelationship(payer: UserEntity,
                          receiver: UserEntity,
                          value: BigDecimal
) : MoneyRelationship(payer, receiver, value) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BalanceRelationship

        if (receiver != other.receiver) return false
        if (payer != other.payer) return false
        if (value != other.value) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

}