package pl.org.pablo.slack.money.slack.request


interface SlackCommandExecutor {
    fun <P : SlackParameter, R : Any> send(command: SlackCommand<P, R>): R
}
