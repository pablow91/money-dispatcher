package pl.org.pablo.slack.money.slack.parser

/**
 * Parsers is design to take finite number of strings and generate result either when enough parameters were specified or parsing was finalized by user.
 *
 * Note: Parser is consider finalized when `parse` command return true or finalize method has been invoked.
 * There is no requirements for methods how they should react to finalized state, so it's not recommended to use then.
 */
interface Parser<out R> {
    /**
     * Check if string can be parsed by parser
     */
    fun canParse(element: String): Boolean

    /**
     * Check is parser have all required data to generate result
     */
    fun canFinalize(): Boolean

    /**
     * Parse string. Should not throws exception when combined with `canParse` invocation.
     * @return if parser is finalized after parsing provided argument
     */
    fun parse(element: String): Boolean

    /**
     * Finalize parsing and generate response
     * @throws IllegalStateException data is in not finalized or consistent state
     * @return object build from parsed data
     */
    fun finalize(): R
}