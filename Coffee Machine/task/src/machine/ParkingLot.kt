package parking

import java.util.*

fun main() {
    val scanner = Scanner(System.`in`)
    var parking: ParkingLot? = null

    loop@ do {
        val cmd = scanner.next()
        when {
            cmd == "create" -> parking = ParkingLot(scanner.nextInt())
            cmd == "exit" -> break@loop
            parking == null -> {
                println("Sorry, a parking lot has not been created.")
                scanner.nextLine()
            }
            cmd == "park" -> parking.park(Car(scanner.next(), scanner.next()))
            cmd == "leave" -> parking.leave(scanner.nextInt())
            cmd == "reg_by_color" -> parking.regByColor(scanner.next())
            cmd == "spot_by_color" -> parking.spotByColor(scanner.next())
            cmd == "spot_by_reg" -> parking.spotByReg(scanner.next())
            cmd == "status" -> parking.status()
            else -> {
                println("Unknown command.")
                scanner.nextLine()
            }
        }
    } while (true)
}

class ParkingLot(capacity: Int) {
    private val parking = arrayOfNulls<Car>(capacity)

    init {
        println("Created a parking lot with $capacity spots.")
    }

    fun park(car: Car) {
        for (i in 0..parking.lastIndex) {
            if (parking[i] == null) {
                parking[i] = car
                println("${car.color} car parked in spot ${i + 1}.")
                return
            }
        }
        println("Sorry, the parking lot is full.")
    }

    fun leave(spot: Int) {
        if (parking[spot - 1] == null) {
            println("There is no car in spot $spot.")
        } else {
            parking[spot - 1] = null
            println("Spot $spot is free.")
        }
    }

    fun regByColor(color: String) {
        println(parking
                .filter { it?.color?.toLowerCase() == color.toLowerCase() }
                .joinToString { it?.number.toString() }
                .ifEmpty { "No cars with color $color were found." }
        )
    }

    fun spotByColor(color: String) {
        println(parking
                .mapIndexed { idx, car -> idx + 1 to car?.color?.toLowerCase() }
                .filter { it.second == color.toLowerCase() }
                .joinToString { it.first.toString() }
                .ifEmpty { "No cars with color $color were found." }
        )
    }

    fun spotByReg(number: String) {
        println(parking
                .mapIndexed { idx, car -> idx + 1 to car?.number?.toLowerCase() }
                .filter { it.second == number.toLowerCase() }
                .joinToString { it.first.toString() }
                .ifEmpty { "No cars with registration number $number were found." }
        )
    }

    fun status() {
        println(parking
                .filterNotNull()
                .mapIndexed { idx, car -> "${idx + 1} ${car.number} ${car.color}" }
                .joinToString(separator = "\n")
                .ifEmpty { "Parking lot is empty." }
        )
    }
}

class Car(val number: String, val color: String)
