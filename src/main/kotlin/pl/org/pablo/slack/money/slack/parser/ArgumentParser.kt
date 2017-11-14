package pl.org.pablo.slack.money.slack.parser

import org.springframework.stereotype.Component

interface Parser<out R> {
    fun canParse(command: String): Boolean
    fun canFinalize(): Boolean
    fun parse(command: String): Boolean
    fun finalize(): R
}

interface ArgumentParser {
    fun <R> parse(parser: Parser<R>, command: String): R
}

@Component
class ArgumentParserImpl : ArgumentParser {
    override fun <R> parse(parser: Parser<R>, command: String): R {
        val commandList = (command.split(" ").toList())
        for (c in commandList) {
            if (!parser.canParse(c)) {
                throw IllegalArgumentException("$c was not expected here is")
            }
            if (parser.parse(c)) {
                return parser.finalize()
            }
        }
        return parser.finalize()
    }

}
