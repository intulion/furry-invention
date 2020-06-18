package flashcards

import java.io.File
import java.util.*

fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)

    var importFile = ""
    var exportFile = ""
    for (i in 0 until args.lastIndex) {
        if (args[i] == "-import") importFile = args[i + 1]
        if (args[i] == "-export") exportFile = args[i + 1]
    }

    val game = Game()
    game.import(importFile)

    loop@ while (true) {
        println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        when (scanner.nextLine()) {
            "add" -> {
                println("The card:")
                val term = scanner.nextLine()
                if (game.isTermExists(term)) {
                    println("The card \"$term\" already exists.\n")
                    continue@loop
                }

                println("The definition of the card:")
                val definition = scanner.nextLine()
                if (game.isDefinitionExists(definition)) {
                    println("The definition \"$definition\" already exists.\n")
                    continue@loop
                }
                game.addCard(term, definition)
            }
            "ask" -> {
                println("How many times to ask?")
                repeat(scanner.nextLine().toInt()) {
                    val card = game.randomCard()
                    println("Print the definition of \"${card.term}\":")
                    println(game.check(card, scanner.nextLine()))
                }
            }
            "remove" -> {
                println("The card:")
                game.removeCard(scanner.nextLine())
            }
            "import" -> {
                println("File name:")
                game.import(scanner.nextLine())
            }
            "export" -> {
                println("File name:")
                game.export(scanner.nextLine())
            }
            "log" -> {
                println("File name:")
                game.saveLog(scanner.nextLine())
            }
            "hardest card" -> game.printHardestCards()
            "reset stats" -> game.resetStats()
            "exit" -> {
                println("Bye bye!")
                break@loop
            }
            else -> println("Unknown command.")
        }
        println()
    }
    game.export(exportFile)
}

class Game {
    private val cards = mutableListOf<Flashcard>()
    private var log = ""

    fun addCard(term: String, definition: String) {
        cards.add(Flashcard(term, definition))
        message("The pair (\"$term\":\"$definition\") has been added.")
    }

    fun removeCard(term: String) {
        val card = cards.find { it.term == term }
        if (card != null) {
            cards.remove(card)
            message("The card has been removed.")
        } else {
            message("Can't remove \"$term\": there is no such card.")
        }
    }

    fun randomCard(): Flashcard {
        val random = Random().nextInt(cards.size)
        return cards[random]
    }

    fun check(card: Flashcard, answer: String): String =
            when {
                card.definition == answer -> "Correct answer."
                cards.any { it.definition == answer } -> {
                    card.mistakes++
                    "Wrong answer. (The correct one is \"${card.definition}\", " +
                            "you've just written the definition of " +
                            "\"${cards.find { it.definition == answer }?.term}\" card)."
                }
                else -> {
                    card.mistakes++
                    "Wrong answer. The correct one is \"${card.definition}\"."
                }
            }

    fun isTermExists(term: String): Boolean =
            cards.any { it.term == term }

    fun isDefinitionExists(definition: String): Boolean =
            cards.any { it.definition == definition }

    private fun message(str: String) {
        log += "$str\n"
        println(str)
    }

    fun printHardestCards() {
        val maxMistakes = cards.maxBy { it.mistakes }?.mistakes ?: 0
        if (maxMistakes > 0) {
            val hardestCards = cards.filter { it.mistakes == maxMistakes }
            if (hardestCards.size == 1) {
                message("The hardest card is \"${hardestCards.first().term}\". " +
                        "You have $maxMistakes errors answering it.")
            } else {
                message("The hardest cards are " +
                        "${hardestCards.joinToString(separator = "\", \"", prefix = "\"", postfix = "\"") { it.term }}. " +
                        "You have $maxMistakes errors answering them.")
            }
        } else {
            message("There are no cards with errors.")
        }
    }

    fun resetStats() {
        cards.forEach {
            it.mistakes = 0
        }
        message("Card statistics has been reset.")
    }

    fun import(fileName: String) {
        if (fileName.isEmpty()) return

        val file = File(fileName)
        if (!file.exists()) {
            message("File not found.")
            return
        }

        var count = 0
        file.forEachLine {
            if (it.isNotEmpty()) {
                val (term, definition, mistakes) = it.split(';')
                val foundCard = cards.find { card -> card.term == term }
                if (foundCard != null) {
                    foundCard.definition = definition
                    foundCard.mistakes = mistakes.toInt()
                } else {
                    cards.add(Flashcard(term, definition, mistakes.toInt()))
                }
                count++
            }
        }
        message("$count cards have been loaded.")
    }

    fun export(fileName: String) {
        if (fileName.isEmpty()) return

        val file = File(fileName)
        file.writeText("")
        cards.forEach {
            file.appendText("${it.term};${it.definition};${it.mistakes}\n")
        }
        message("${cards.size} cards have been saved.")
    }

    fun saveLog(fileName: String) {
        if (fileName.isEmpty()) return

        val file = File(fileName)
        file.writeText(log.trim())
        message("The log has been saved.")
    }
}

class Flashcard(val term: String, var definition: String, var mistakes: Int = 0)
