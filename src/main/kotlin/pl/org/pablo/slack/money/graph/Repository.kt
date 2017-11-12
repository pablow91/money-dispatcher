package pl.org.pablo.slack.money.graph

import org.springframework.data.neo4j.annotation.Depth
import org.springframework.data.neo4j.repository.Neo4jRepository

interface UserRepository : Neo4jRepository<UserEntity, Long> {
    fun findByName(name: String, @Depth depth: Int = 1): UserEntity?
}

interface MoneyRelationshipRepository : Neo4jRepository<MoneyRelationship, Long>
