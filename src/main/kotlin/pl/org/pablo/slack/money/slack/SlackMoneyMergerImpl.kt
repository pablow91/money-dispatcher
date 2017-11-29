package pl.org.pablo.slack.money.slack

import org.springframework.stereotype.Component
import pl.org.pablo.slack.money.money.AddDto
import pl.org.pablo.slack.money.slack.parser.AddSingleArgument
import pl.org.pablo.slack.money.withMoneyScale

@Component
class SlackMoneyMergerImpl : SlackMoneyMerger {

    override fun mergeUserIntoSequence(payments: List<AddSingleArgument>, from: String, description: String?) = payments.asSequence()
            .flatMap { toAddDto(from, it, description) }
            .groupBy { it.to }
            .asSequence()
            .map { mergeOneUserAddDto(it.value) }

    private fun toAddDto(from: String, arg: AddSingleArgument, desc: String?): Sequence<AddDto> {
        var perUserValue = arg.value
        if (arg.options.contains("eq")) {
            perUserValue /= arg.users.size.toBigDecimal()
        }
        return arg.users.asSequence().map { AddDto(from, it, perUserValue, desc) }
    }

    private fun mergeOneUserAddDto(value: List<AddDto>): AddDto {
        val dto = value.asSequence().reduce { acc, addDto -> acc.copy(value = acc.value + addDto.value) }
        return dto.copy(value = dto.value.withMoneyScale())
    }

}