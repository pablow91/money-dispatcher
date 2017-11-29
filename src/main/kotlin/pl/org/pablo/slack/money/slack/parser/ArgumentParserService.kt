package pl.org.pablo.slack.money.slack.parser

/**
 * Service for parsing slack command with provided parser
 */
interface ArgumentParserService {
    /**
     * Parsing command with provided parser
     *
     * Parser might return value before parsing whole command
     *
     * @throws IllegalArgumentException one of command element cannot be parsed by parser
     * @throws IllegalStateException parser is forced to generate result, but data are either not complete or inconsistent
     *
     * @return object that is build based on provided command
     */
    fun <R> parse(parser: Parser<R>, command: String): R
}
