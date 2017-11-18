package pl.org.pablo.slack.money.slack.parser

class AddArgumentParser : Parser<AddArgument> {

    private val userParser = AddUserParser()
    private val descriptionParser = AddDescriptionParser()

    private var users: List<AddSingleArgument>? = null

    override fun canParse(element: String): Boolean {
        if (users == null) {
            return userParser.canParse(element) || userParser.canFinalize()
        }
        return true
    }

    override fun canFinalize(): Boolean = users != null || userParser.canFinalize()

    override fun parse(element: String): Boolean {
        if (users == null) {
            if (userParser.canParse(element)) {
                userParser.parse(element)
                return false
            } else {
                users = userParser.finalize()
            }
        }
        descriptionParser.parse(element)
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

    override fun canParse(element: String): Boolean {
        val type = elementType(element)
        return when (type) {
            Status.USER -> true
            Status.VALUE -> current.users.isNotEmpty()
            Status.OPTION -> true
            Status.UNKNOWN -> false
        }
    }

    override fun canFinalize(): Boolean = result.isNotEmpty() && current.isEmpty()

    private object Pattern {
        val user = Regex("<@([aA-zZ0-9]+)(\\|[aA-zZ0-9]+)?>")
        val value = Regex("\\d+")
        val option = Regex("-+[aA-zZ0-9]+")
    }

    private fun elementType(element: String): Status = when (element) {
        in Pattern.user -> Status.USER
        in Pattern.value -> Status.VALUE
        in Pattern.option -> Status.OPTION
        else -> Status.UNKNOWN
    }

    override fun parse(element: String): Boolean {
        val type = elementType(element)
        when (type) {
            Status.USER -> parseUser(element)
            Status.VALUE -> parseValue(element)
            Status.OPTION -> parseOption(element)
            Status.UNKNOWN -> throw IllegalArgumentException("Illegal `parse` invocation. Use `canParse` earlier")
        }
        return false
    }

    private fun parseUser(userName: String) {
        val name = userName.replace(Pattern.user, { it.destructured.component1() })
        current.users.add(name)
    }

    private fun parseValue(value: String) {
        val lastValue = value.toInt()
        val res = AddSingleArgument(current.users.toList(), lastValue, current.options.toSet())
        result.add(res)
        current = AddPaymentForUsers()
    }

    private fun parseOption(opt: String) {
        val option = opt.dropWhile { it == '-' }
        current.options.add(option)
    }

    override fun finalize(): List<AddSingleArgument> {
        if (!canFinalize()) {
            throw IllegalStateException("Parser is in inconsistent state")
        }
        return result.toList()
    }

}

class AddDescriptionParser : Parser<String?> {
    private val sb = StringBuilder()

    override fun canParse(element: String): Boolean = true

    override fun canFinalize(): Boolean = true

    override fun parse(element: String): Boolean {
        sb.append(element).append(" ")
        return false
    }

    override fun finalize(): String? = sb.toString()
            .trim()
            .let { if (it.isBlank()) null else it }
}

operator fun Regex.contains(text: CharSequence): Boolean = this.matches(text)
