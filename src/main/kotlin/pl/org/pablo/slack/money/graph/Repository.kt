package pl.org.pablo.slack.money.graph

import org.springframework.data.neo4j.annotation.Depth
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.repository.query.Param

interface UserRepository : Neo4jRepository<UserEntity, Long> {
    fun findByName(name: String, @Depth depth: Int = 1): UserEntity?
}

interface MoneyRelationshipRepository : Neo4jRepository<MoneyRelationship, Long> {
    @Query("MATCH (n:UserEntity)-[r:BALANCE]->(:UserEntity)\n" +
            "OPTIONAL MATCH (n)-[p:PAY]->(:UserEntity)\n" +
            "WITH n, p, coalesce(last(extract(var IN collect(p) | var.creationDate)), \"2000-01-01T00:00\") as lastPayment\n" +
            "WITH n, lastPayment,\n" +
            "\treduce(acc = 0, v IN filter(var IN collect(p) WHERE var.creationDate <= lastPayment) | acc + v.value) as payed\n" +
            "OPTIONAL MATCH (:UserEntity)-[p1:PAY]->(n)\n" +
            "WITH n, lastPayment, payed,\n" +
            "\treduce(acc = 0, v IN filter(var IN collect(p1) WHERE var.creationDate <= lastPayment) | acc + v.value) as received,\n" +
            "    any(var IN collect(p1) WHERE var.creationDate <= {date}) as bla\n" +
            "WHERE lastPayment < {date} AND (payed < received) OR (payed = 0 AND received = 0 AND bla)\n" +
            "RETURN n.name")
    fun findNotPayingUsers(@Param("date") date: String): List<String>

}
