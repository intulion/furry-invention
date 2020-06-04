package machine

import java.util.*


fun main() {
    val scanner = Scanner(System.`in`)
    val machine = CoffeeMachine(400, 540, 120, 9, 550)

    while (machine.state != State.EXIT) {
        if (machine.state.isWaiting) {
            val input = scanner.next()
            machine.process(input)
        } else {
            machine.process("")
        }
    }
}

class CoffeeMachine(
        private var water: Int,
        private var milk: Int,
        private var beans: Int,
        private var cups: Int,
        private var money: Int
) {
    var state = State.MSG_MENU

    fun process(input: String) {
        when (state) {
            State.MSG_STATUS -> showStatus()
            State.MSG_MENU -> showMenu()
            State.MSG_BUY -> showBuyMenu()
            State.MSG_WATER, State.MSG_MILK, State.MSG_BEANS, State.MSG_CUPS -> showFillMessage()
            State.WAIT_MENU -> selectMenu(input)
            State.WAIT_BUY -> buy(input)
            State.WAIT_WATER, State.WAIT_MILK, State.WAIT_BEANS, State.WAIT_CUPS -> fill(input)
            State.TAKE -> takeMoney()
            else -> state = State.MSG_STATUS
        }
    }

    private fun showStatus() {
        println("The coffee machine has:")
        println("$water of water")
        println("$milk of milk")
        println("$beans of coffee beans")
        println("$cups of disposable cups")
        println("$money of money")
        state = State.getNextState(state)
    }

    private fun showMenu() {
        println()
        print("Write action (buy, fill, take, remaining, exit): > ")
        state = State.getNextState(state)
    }

    private fun selectMenu(input: String) {
        when (input) {
            "buy" -> state = State.MSG_BUY
            "fill" -> state = State.MSG_WATER
            "take" -> state = State.TAKE
            "remaining" -> state = State.MSG_STATUS
            "exit" -> state = State.EXIT
        }

        println()
    }

    private fun showBuyMenu() {
        print("What do you want to buy? 1 - espresso, 2 - latte, 3 - cappuccino, back - to main menu: > ")
        state = State.getNextState(state)
    }

    private fun buy(input: String) {
        when (input) {
            "1" -> makeCoffee(250, 0, 16, 4) // Espresso
            "2" -> makeCoffee(350, 75, 20, 7) // Latte
            "3" -> makeCoffee(200, 100, 12, 6) // Cappuccino
        }

        state = State.getNextState(state)
    }

    private fun makeCoffee(water: Int, milk: Int, beans: Int, money: Int) {
        when {
            this.water < water -> println("Sorry, not enough water!")
            milk < milk -> println("Sorry, not enough milk!")
            this.beans < beans -> println("Sorry, not enough beans!")
            this.cups < 1 -> println("Sorry, not enough cups!")
            else -> {
                this.water -= water
                this.milk -= milk
                this.beans -= beans
                this.cups -= 1
                this.money += money
                println("I have enough resources, making you a coffee!")
            }
        }
    }

    private fun showFillMessage() {
        when (state) {
            State.MSG_WATER -> print("Write how many ml of water do you want to add: > ")
            State.MSG_MILK -> print("Write how many ml of milk do you want to add: > ")
            State.MSG_BEANS -> print("Write how many grams of coffee beans do you want to add: > ")
            State.MSG_CUPS -> print("Write how many disposable cups of coffee do you want to add: > ")
            else -> state = State.MSG_STATUS
        }
        state = State.getNextState(state)
    }

    private fun fill(input: String) {
        when (state) {
            State.WAIT_WATER -> water += input.toInt()
            State.WAIT_MILK -> milk += input.toInt()
            State.WAIT_BEANS -> beans += input.toInt()
            State.WAIT_CUPS -> cups += input.toInt()
            else -> state = State.MSG_STATUS
        }
        state = State.getNextState(state)
    }

    private fun takeMoney() {
        println("I gave you \$$money")
        money = 0
        state = State.getNextState(state)
    }
}

enum class State(val isWaiting: Boolean) {
    MSG_STATUS(false),
    MSG_MENU(false),
    WAIT_MENU(true),
    MSG_BUY(false),
    WAIT_BUY(true),
    MSG_WATER(false),
    MSG_MILK(false),
    MSG_BEANS(false),
    MSG_CUPS(false),
    WAIT_WATER(true),
    WAIT_MILK(true),
    WAIT_BEANS(true),
    WAIT_CUPS(true),
    FILL(false),
    TAKE(false),
    EXIT(false);

    companion object {
        fun getNextState(state: State): State {
            return when (state) {
                MSG_STATUS -> MSG_MENU
                MSG_MENU -> WAIT_MENU
                WAIT_BUY -> MSG_MENU
                MSG_BUY -> WAIT_BUY
                FILL -> MSG_WATER
                TAKE -> MSG_MENU
                MSG_WATER -> WAIT_WATER
                MSG_MILK -> WAIT_MILK
                MSG_BEANS -> WAIT_BEANS
                MSG_CUPS -> WAIT_CUPS
                WAIT_WATER -> MSG_MILK
                WAIT_MILK -> MSG_BEANS
                WAIT_BEANS -> MSG_CUPS
                WAIT_CUPS -> MSG_MENU
                else -> MSG_STATUS
            }
        }
    }
}
