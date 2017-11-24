package pl.org.pablo.slack.money.slack.interactive

enum class CallbacksId(val const: String) {
    ConfirmAdd("confirm_add");

    companion object {
        private val cache = CallbacksId.values().asSequence()
                .map { Pair(it.const, it) }
                .toMap()

        fun from(value: String): CallbacksId? = cache[value]
    }
}