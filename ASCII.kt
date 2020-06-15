package signature

import java.io.File
import java.util.*

fun main() {
    val scanner = Scanner(System.`in`)

    val mediumFont = readFont("C:\\tmp\\medium.txt")
    val romanFont = readFont("C:\\tmp\\roman.txt")

    print("Enter name and surname: > ")
    val name = scanner.nextLine()
    print("Enter person's status: > ")
    val status = scanner.nextLine()

    val output = mutableListOf<String>()
    fillOutput(output, romanFont, name)
    fillOutput(output, mediumFont, status)
    addFrame(output)

    for (line in output) {
        println(line)
    }
}

fun readFont(filePath: String): MutableMap<Char, MutableList<String>> {
    val font = mutableMapOf<Char, MutableList<String>>()

    val scanner = Scanner(File(filePath))
    val height = scanner.nextInt()
    val count = scanner.nextInt()

    for (i in 1..count) {
        val ch = scanner.next().first()
        val width = scanner.nextInt()

        // add space
        if (ch == 'a') {
            val spaceList = mutableListOf<String>()
            for (j in 1..height) {
                spaceList.add(" ".repeat(width))
            }
            font[' '] = spaceList
        }

        scanner.nextLine()

        val strList = mutableListOf<String>()
        for (j in 1..height) {
            strList.add(scanner.nextLine())
        }
        font[ch] = strList
    }

    return font
}

fun fillOutput(output: MutableList<String>, font: MutableMap<Char, MutableList<String>>, word: String) {
    val firstLine = output.size
    val height = font.getValue(' ').size
    for (i in 0 until height) {
        output.add("")
    }

    for (ch in word) {
        for (i in 0 until height) {
            output[i + firstLine] += font.getValue(ch)[i]
        }
    }
}

fun addFrame(output: MutableList<String>) {
    val maxWidth = output.maxBy { it.length }!!.length

    for (i in 0..output.lastIndex) {
        val addSpaces = maxWidth - output[i].length
        val leftSpaces = addSpaces / 2
        val rightSpaces = if(addSpaces % 2 == 0) leftSpaces else leftSpaces + 2 - addSpaces % 2
        output[i] = "88  " + " ".repeat(leftSpaces) + output[i] + " ".repeat(rightSpaces) + "  88"
    }

    output.add(0, "8".repeat(maxWidth + 8))
    output.add("8".repeat(maxWidth + 8))
}
