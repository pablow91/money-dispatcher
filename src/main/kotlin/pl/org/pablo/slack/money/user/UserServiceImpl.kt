package pl.org.pablo.slack.money.user

import org.springframework.stereotype.Component
import pl.org.pablo.slack.money.graph.UserEntity
import pl.org.pablo.slack.money.graph.UserRepository

@Component
class UserServiceImpl(
        private val userRepository: UserRepository
) : UserService {
    override fun getOrCreate(name: String): UserEntity =
            userRepository.findByName(name) ?: userRepository.save(UserEntity(name))

    override fun cleanAll() {
        userRepository.deleteAll()
    }

}
