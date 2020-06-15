package converter

import java.lang.Exception
import java.util.*

fun main() {
    val scanner = Scanner(System.`in`).useLocale(Locale.US)

    while (true) {
        print("Enter what you want to convert (or exit): ")
        val input = scanner.nextLine().toLowerCase().split(' ')
        if (input.first() == "exit") break

        // parse
        val value: Double
        val source: String
        val dest: String
        try {
            var idx = 0
            value = input[idx++].toDouble() // number
            source = if (input[idx].startsWith("degree")) "${input[idx++]} ${input[idx++]}" else input[idx++]
            idx++ // random word like "to" or "in"
            dest = if (input[idx].startsWith("degree")) "${input[idx++]} ${input[idx]}" else input[idx]
        } catch (e: Exception) {
            println("Parse error")
            continue
        }

        val fromUnit = Unit.getUnit(source)
        val toUnit = Unit.getUnit(dest)

        // checks
        if (fromUnit == Unit.NotFound || toUnit == Unit.NotFound || fromUnit.getBaseUnit() != toUnit.getBaseUnit()) {
            println("Conversion from ${fromUnit.plural} to ${toUnit.plural} is impossible")
            continue
        }

        if (fromUnit.type.isNotEmpty() && value < 0) {
            println("${fromUnit.type} shouldn't be negative")
            continue
        }

        // convert
        val result = fromUnit.convert(toUnit, value)
        println("${fromUnit.present(value)} is ${toUnit.present(result)}")
    }
}

enum class Unit(val type: String, val keywords: String, val singular: String, val plural: String, private val ratio: Double) {
    NotFound("", "", "???", "???", 1.0),
    Meter("Length", "m", "meter", "meters", 1.0),
    Kilometer("Length", "km", "kilometer", "kilometers", 1000.0),
    Centimeter("Length", "cm", "centimeter", "centimeters", 0.01),
    Millimeter("Length", "mm", "millimeter", "millimeters", 0.001),
    Mile("Length", "mi", "mile", "miles", 1609.35),
    Yard("Length", "yd", "yard", "yards", 0.9144),
    Foot("Length", "ft", "foot", "feet", 0.3048),
    Inch("Length", "in", "inch", "inches", 0.0254),
    Gram("Weight", "g", "gram", "grams", 1.0),
    Kilogram("Weight", "kg", "kilogram", "kilograms", 1000.0),
    Milligram("Weight", "mg", "milligram", "milligrams", 0.001),
    Pound("Weight", "lb", "pound", "pounds", 453.592),
    Ounce("Weight", "oz", "ounce", "ounces", 28.3495),
    Kelvin("", "k", "Kelvin", "Kelvins", 1.0),
    Celsius("", "c, celsius, dc", "degree Celsius", "degrees Celsius", 1.0),
    Fahrenheit("", "f, fahrenheit, df", "degree Fahrenheit", "degrees Fahrenheit", 1.0);

    fun present(value: Double): String =
            "$value ${if (value == 1.0) this.singular else this.plural}"

    fun convert(toUnit: Unit, value: Double): Double =
            when (this) {
                NotFound -> this.ratio
                Kelvin, Celsius, Fahrenheit -> {
                    val result = when (this) {
                        Celsius -> value + 273.15
                        Fahrenheit -> (value + 459.67) * 5 / 9
                        else -> value
                    }

                    when (toUnit) {
                        Celsius -> result - 273.15
                        Fahrenheit -> result * 9 / 5 - 459.67
                        else -> result
                    }
                }
                else -> value * this.ratio / toUnit.ratio
            }

    fun getBaseUnit(): Unit =
        when (this) {
            Meter, Kilometer, Centimeter, Millimeter, Mile, Yard, Foot, Inch -> Meter
            Gram, Kilogram, Milligram, Pound, Ounce -> Gram
            else -> NotFound
        }

    companion object {
        fun getUnit(string: String): Unit =
                values().find {
                    it.keywords.split(", ").contains(string)
                            || it.singular.toLowerCase() == string
                            || it.plural.toLowerCase() == string
                } ?: NotFound
    }
}
