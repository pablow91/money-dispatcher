package pl.org.pablo.slack.money.slack.parser

class AddArgumentParser : Parser<AddArgument> {

    private val userParser = AddUserParser()
    private val descriptionParser = AddDescriptionParser()

    private var users: List<AddSingleArgument>? = null

    override fun canParse(command: String): Boolean {
        if (users == null) {
            return userParser.canParse(command) || userParser.canFinalize()
        }
        return true
    }

    override fun canFinalize(): Boolean = users != null || userParser.canFinalize()

    override fun parse(command: String): Boolean {
        if (users == null) {
            if (userParser.canParse(command)) {
                userParser.parse(command)
                return false
            } else {
                users = userParser.finalize()
            }
        }
        descriptionParser.parse(command)
        return false
    }


    override fun finalize(): AddArgument {
        if (!canFinalize()) {
            throw IllegalStateException()
        }
        val uRet = users ?: userParser.finalize()
        val dRet = descriptionParser.finalize()
        return AddArgument(uRet, dRet)
    }

}

data class AddSingleArgument(
        val users: List<String>,
        val value: Int,
        val options: Set<String> = emptySet()
)

data class AddArgument(
        val payments: List<AddSingleArgument>,
        val description: String? = null
)

class AddUserParser : Parser<List<AddSingleArgument>> {

    data class AddPaymentForUsers(
            val users: MutableList<String> = arrayListOf(),
            val options: MutableSet<String> = hashSetOf()
    ) {
        fun isEmpty() = users.isEmpty() && options.isEmpty()
    }


    private val result = arrayListOf<AddSingleArgument>()

    private var current = AddPaymentForUsers()

    enum class Status {
        USER, VALUE, OPTION, UNKNOWN
    }

    override fun canParse(command: String): Boolean {
        val type = commandType(command)
        return when (type) {
            Status.USER -> true
            Status.VALUE -> current.users.isNotEmpty()
            Status.OPTION -> true
            Status.UNKNOWN -> false
        }
    }

    override fun canFinalize(): Boolean = result.isNotEmpty() && current.isEmpty()

    private fun commandType(command: String) = when (command) {
        in Regex("<@([aA-zZ0-9]+)(\\|[aA-zZ0-9]+)?>") -> Status.USER
        in Regex("\\d+") -> Status.VALUE
        in Regex("-+[aA-zZ0-9]+") -> Status.OPTION
        else -> Status.UNKNOWN
    }

    override fun parse(command: String): Boolean {
        val type = commandType(command)
        when (type) {
            Status.USER -> parseUser(command)
            Status.VALUE -> parseValue(command)
            Status.OPTION -> parseOption(command)
            Status.UNKNOWN -> throw IllegalArgumentException()
        }
        return false
    }

    private fun parseUser(userName: String) {
        val name = userName.replace(Regex("<@([aA-zZ0-9]+)(\\|[aA-zZ0-9]+)?>"), { it.destructured.component1() })
        current.users.add(name)
    }

    private fun parseValue(value: String) {
        if (current.users.isEmpty()) {
            throw IllegalStateException("At least one user has to be provided")
        }
        val lastValue = value.toInt()
        val res = AddSingleArgument(current.users.toList(), lastValue, current.options.toSet())
        result.add(res)
        current = AddPaymentForUsers()
    }

    private fun parseOption(command: String) {
        val option = command.dropWhile { it == '-' }
        current.options.add(option)
    }

    override fun finalize(): List<AddSingleArgument> {
        if (!canFinalize()) {
            throw IllegalStateException("Value was required, but last")
        }
        return result.toList()
    }

}

class AddDescriptionParser : Parser<String?> {
    private val sb = StringBuilder()

    override fun canParse(command: String): Boolean = true

    override fun canFinalize(): Boolean = true

    override fun parse(command: String): Boolean {
        sb.append(command).append(" ")
        return false
    }

    override fun finalize(): String? = sb.toString()
            .trim()
            .let { if (it.isBlank()) null else it }
}

operator fun Regex.contains(text: CharSequence): Boolean = this.matches(text)
