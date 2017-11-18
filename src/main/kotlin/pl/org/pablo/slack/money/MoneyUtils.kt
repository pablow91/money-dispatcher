package pl.org.pablo.slack.money

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.withMoneyScale(): BigDecimal = setScale(2, RoundingMode.HALF_UP)
fun String.toMoneyDecimal() = toBigDecimal().withMoneyScale()