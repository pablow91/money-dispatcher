package pl.org.pablo.slack.money.user

import pl.org.pablo.slack.money.graph.UserEntity

interface UserService {
    fun getOrCreate(name: String): UserEntity

    fun cleanAll()

}
