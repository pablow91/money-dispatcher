package pl.org.pablo.slack.money.slack.parser

import org.springframework.stereotype.Service

@Service
class ArgumentParserServiceImpl : ArgumentParserService {
    override fun <R> parse(parser: Parser<R>, command: String): R {
        val elementList = command.split(" ").toList()
        for (element in elementList) {
            if (!parser.canParse(element)) {
                throw IllegalArgumentException("$element was not expected here")
            }
            if (parser.parse(element)) {
                return parser.finalize()
            }
        }
        return parser.finalize()
    }

}