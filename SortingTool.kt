package sorting

import java.io.File
import java.util.*

fun main(args: Array<String>) {
    try {
        val sorter = Sorter()
        sorter.setParams(args.toList())
        sorter.run()
    } catch (e: Exception) {
        println(e.message)
    }
}

class Sorter {
    private val validSortingTypes = listOf("natural", "byCount")
    private val validDataTypes = listOf("long", "line", "word")
    private val params = mutableMapOf("-sortingType" to "natural", "-dataType" to "", "-inputFile" to "", "-outputFile" to "")
    private var dataEntries = mutableListOf<Any>()
    private var dataEntryToCount = mutableMapOf<Any, Int>().withDefault { 0 }
    private var outputFile: File? = null

    fun setParams(args: List<String>) {
        args.chunked(2).forEach { pair ->
            when {
                !params.containsKey(pair[0]) -> message("\"${pair[0]}\" isn't a valid parameter. It's skipped.")
                pair[0] == "-sortingType" && (pair.size == 1 || !validSortingTypes.any { it == pair[1] }) ->
                    throw(Exception("No sorting type defined!"))
                pair[0] == "-dataType" && (pair.size == 1 || !validDataTypes.any { it == pair[1] }) ->
                    throw(Exception("No data type defined!"))
                else -> params[pair[0]] = pair[1]
            }
        }

        if (!params["-inputFile"].isNullOrEmpty()) params["-dataType"] = "long"

        val outputFileName = params["-outputFile"] ?: ""
        if (outputFileName.isNotEmpty()) {
            outputFile = File(outputFileName)
            outputFile?.writeText("")
        }
    }

    fun run() {
        readEntries()
        sortEntries()
        printOutput()
    }

    private fun readEntries() {
        val fileName = params["-inputFile"] ?: ""
        val scanner = if (fileName.isEmpty()) {
            Scanner(System.`in`)
        } else {
            val file = File(fileName)
            Scanner(file.readText())
        }
        while (scanner.hasNext()) {
            when (params["-dataType"]) {
                "long" -> {
                    val num = scanner.next()
                    try {
                        dataEntries.add(num.toInt())
                    } catch (e: Exception) {
                        message("\"$num\" isn't a long. It's skipped.\n")
                    }
                }
                "line" -> dataEntries.add(scanner.nextLine())
                "word" -> dataEntries.add(scanner.next())
            }
        }
        scanner.close()
    }

    private fun sortEntries() {
        when (params["-dataType"]) {
            "long" -> {
                dataEntries = dataEntries.map { it as Int }.sorted().toMutableList()
            }
            "line" -> {
                val descLengthComparator = Comparator { str1: String, str2: String -> str2.length - str1.length }
                dataEntries = dataEntries.map { it as String }.sortedWith(descLengthComparator).toMutableList()
            }
            "word" -> {
                dataEntries = dataEntries.map { it as String }.sorted().toMutableList()
            }
        }

        dataEntries.forEach {
            dataEntryToCount[it] = dataEntryToCount.getValue(it) + 1
        }
    }

    private fun printOutput() {
        val name = when (params["-dataType"]) {
            "long" -> "numbers"
            "line" -> "lines"
            "word" -> "words"
            else -> "entries"
        }
        message("Total $name: ${dataEntries.size}")

        if (params["-sortingType"] == "natural") {
            val separator = if (params["-dataType"] == "line") "\n" else " "
            message("Sorted data:$separator${dataEntries.joinToString(separator)}")
        } else {
            dataEntryToCount
                    .toList()
                    .sortedWith(compareBy({ it.second }, {
                        if (params["-dataType"] == "long") it.first as Int else it.first as String
                    }))
                    .sortedBy { (_, value) -> value }
                    .forEach { (entry, count) ->
                        message("$entry: $count time(s), ${100 * count / dataEntries.size}%")
                    }
        }
    }

    private fun message(str: String) {
        outputFile?.appendText("$str\n") ?: println(str)
    }
}
