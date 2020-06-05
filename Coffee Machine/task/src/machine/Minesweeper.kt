package minesweeper

import java.util.*
import kotlin.random.Random

fun main() {
    val scanner = Scanner(System.`in`)

    val minefield = Minefield(9, 9)

    print("How many mines do you want on the field? > ")
    minefield.minesNum = scanner.nextInt()
    minefield.seedMines()
    minefield.calculateMines()
    minefield.printField()

    while (!minefield.stop()) {
        print("Set/unset mines marks or claim a cell as free: > ")
        val x = scanner.nextInt()
        val y = scanner.nextInt()
        val cmd = scanner.next()

        if (cmd == "mine") {
            minefield.markCell(x, y)
        } else {
            minefield.claimCell(x, y)
        }
        minefield.printField()
    }

    if (minefield.failed) {
        println("You stepped on a mine and failed!")
    } else {
        println("Congratulations! You found all the mines!")
    }
}

class Minefield(private val width: Int, private val height: Int) {
    private val field: Array<Array<Cell>> = Array(height) { Array(width) { Cell() } }
    var minesNum = 0
    var markedMines = 0
    var unexploredCells = 0
    var failed = false

    fun seedMines() {
        var counter = 0
        while (counter < minesNum) {
            val x = Random.nextInt(0, width)
            val y = Random.nextInt(0, height)

            if (field[x][y].type == CellType.FREE) {
                field[x][y].type = CellType.MINE
                counter++
            }
        }
    }

    fun calculateMines() {
        for (i in 0 until height) {
            for (j in 0 until width) {
                if (field[i][j].type == CellType.MINE) continue
                field[i][j].value = countMinesAround(i, j)
            }
        }
    }

    fun printField() {
        for (y in 0..field.lastIndex) {
            if (y == 0) {
                println()
                println(" │123456789│")
                println("—│—————————│")
            }

            for (x in 0..field[y].lastIndex) {
                val cell = field[y][x]
                when (x) {
                    0 -> print("${y + 1}│${cell.getSymbol()}")
                    field[y].lastIndex -> println("${cell.getSymbol()}|")
                    else -> print(cell.getSymbol())
                }
            }
            if (y == field.lastIndex) println("—│—————————│")
        }
    }

    fun markCell(x: Int, y: Int) {
        val cell = field[y - 1][x - 1]
        cell.mark()
        updateStatistic()
    }

    fun claimCell(x: Int, y: Int) {
        failed = field[y - 1][x - 1].claim()
        if (failed) exploreAll() else exploreCellsAround(y - 1, x - 1)
        updateStatistic()
    }

    fun stop(): Boolean {
        return markedMines == minesNum || unexploredCells == minesNum || failed
    }

    private fun countMinesAround(i: Int, j: Int): Int {
        var result = 0
        for (x in -1..1) {
            for (y in -1..1) {
                val x1 = i + x
                val y1 = j + y
                val skipCell = (x == 0 && y == 0)
                        || x1 < 0
                        || y1 < 0
                        || x1 > field.lastIndex
                        || y1 > field[j].lastIndex

                result += if (!skipCell && field[x1][y1].type == CellType.MINE) 1 else 0
            }
        }
        return result
    }

    private fun exploreCellsAround(i: Int, j: Int) {
        if (field[i][j].value != 0) return

        for (x in -1..1) {
            for (y in -1..1) {
                val x1 = i + x
                val y1 = j + y
                val skipCell = (x == 0 && y == 0)
                        || x1 < 0
                        || y1 < 0
                        || x1 > field.lastIndex
                        || y1 > field[j].lastIndex

                if (skipCell || field[x1][y1].status == CellStatus.EXPLORED) continue

                val cell = field[x1][y1]
                cell.status = CellStatus.EXPLORED
                if (cell.type == CellType.FREE && cell.value == 0) exploreCellsAround(x1, y1)
            }
        }
    }

    private fun exploreAll() {
        for (i in 0 until height) {
            for (j in 0 until width) {
                field[i][j].status = CellStatus.EXPLORED
            }
        }
    }

    private fun updateStatistic() {
        markedMines = 0
        unexploredCells = 0

        for (i in 0 until height) {
            for (j in 0 until width) {
                val cell = field[i][j]
                if (cell.status == CellStatus.MARKED && cell.type == CellType.MINE) markedMines++
                if (cell.status != CellStatus.EXPLORED) unexploredCells++
            }
        }
    }
}

data class Cell(
        var value: Int = 0,
        var type: CellType = CellType.FREE,
        var status: CellStatus = CellStatus.UNEXPLORED
) {
    fun getSymbol(): String = when {
        status == CellStatus.UNEXPLORED -> "."
        status == CellStatus.MARKED -> "*"
        status == CellStatus.EXPLORED && type == CellType.MINE -> "X"
        status == CellStatus.EXPLORED && type == CellType.FREE && value == 0 -> "/"
        else -> value.toString()
    }

    fun mark() {
        if (status == CellStatus.UNEXPLORED) status = CellStatus.MARKED else status = CellStatus.UNEXPLORED
    }

    fun claim(): Boolean {
        status = CellStatus.EXPLORED
        return type == CellType.MINE
    }
}

enum class CellType {
    FREE, MINE
}

enum class CellStatus {
    UNEXPLORED, EXPLORED, MARKED
}
