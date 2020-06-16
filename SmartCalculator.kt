package calculator

import java.math.BigInteger
import java.util.*

fun main() {
    val scanner = Scanner(System.`in`)
    val calculator = Calculator()

    program@ while (true) {
        val str = scanner.nextLine().replace("\\s+".toRegex(), " ")
        when {
            str.isEmpty() -> continue@program
            str == "/help" -> {
                println("The program calculates arithmetic operations (+, -, *, /) " +
                        "with very large numbers as well as parentheses to change the priority within an expression.")
                continue@program
            }
            str == "/exit" -> {
                println("Bye!")
                break@program
            }
            str.startsWith('/') -> {
                println("Unknown command")
                continue@program
            }
        }

        try {
            calculator.process(str)
        } catch (e: Exception) {
            println(e.message ?: "Invalid expression")
        }
    }
}

class Calculator {
    private val variables = mutableMapOf<String, BigInteger>()

    fun process(str: String) {
        val statements = str.split('=')

        when (statements.size) {
            1 -> println(calculate(statements.first().trim()))
            2 -> assign(statements.first().trim(), statements.last().trim())
            else -> throw Exception("Invalid assignment")
        }
    }

    private fun assign(varName: String, expression: String) {
        if (!isVarName(varName)) throw Exception("Invalid identifier")

        try {
            variables[varName] = calculate(expression)
        } catch (e: Exception) {
            throw Exception("Invalid assignment")
        }
    }

    private fun calculate(expression: String): BigInteger {
        val list = infixToPostfix(expression)
        val stack = Stack<BigInteger>()

        for (element in list) {
            if (Operator.isOperator(element)) {
                val a = stack.pop()
                val b = stack.pop()
                when (element) {
                    Operator.ADD.sign -> stack.push(b + a)
                    Operator.SUB.sign -> stack.push(b - a)
                    Operator.MULT.sign -> stack.push(b * a)
                    Operator.DIV.sign -> stack.push(b / a)
                    Operator.POW.sign -> stack.push(b.pow(a.toInt()))
                }
            } else {
                stack.push(getValue(element))
            }
        }

        return stack.pop()
    }

    private fun isVarName(name: String) =
            name.first().isLetter() && name.matches("[a-zA-Z]+".toRegex())

    private fun getValue(str: String): BigInteger =
            if (isVarName(str)) {
                if (!variables.containsKey(str)) throw Exception("Unknown variable")
                variables.getValue(str)
            } else {
                str.toBigInteger()
            }

    private fun splitExp(exp: String): List<String> {
        val clearExp = exp
                .replace("\\+{2,}".toRegex(), "+") // plus
                .replace("([^-+]|^)(?:[+]*-[+]*-)*[+]*-[+]*([^+-])".toRegex(), "$1-$2") // odd minus
                .replace("([^-+]|^)(?:[+]*-[+]*-[+]*)+([^+-])".toRegex(), "$1+$2") // even minus
                .replace("^-|([\\^(+\\-*/])\\s*-".toRegex(), "$1#") // unary minus
        return "([#?.\\w]+)|[\\^()+\\-*/]".toRegex().findAll(clearExp)
                .map { it.value.replace('#', '-') }
                .toList()
    }

    private fun infixToPostfix(exp: String): List<String> {
        val infixList = splitExp(exp)
        val postfixList = mutableListOf<String>()
        val stack = Stack<String>()

        for (element in infixList) {
            when {
                !Operator.isOperator(element) -> postfixList.add(element)
                stack.isEmpty() -> stack.push(element)
                stack.peek() == "(" && element == ")" -> stack.pop()
                stack.peek() == "(" -> stack.push(element)
                element == "(" -> stack.push(element)
                element == ")" -> {
                    while (stack.peek() != "(") {
                        postfixList.add(stack.pop())
                    }
                    stack.pop()
                }
                element.priority() > stack.peek().priority() -> stack.push(element)
                element.priority() <= stack.peek().priority() -> {
                    loop@ do {
                        postfixList.add(stack.pop())
                        if (stack.isEmpty()) break@loop
                    } while (element.priority() <= stack.peek().priority() && stack.peek() != "(")
                    stack.push(element)
                }
            }
        }

        while (!stack.isEmpty()) {
            if (stack.peek() == "(" || stack.peek() == ")") throw Exception("Invalid expression")
            postfixList.add(stack.pop())
        }

        return postfixList
    }
}

fun String.priority() = Operator.getPriority(this)

enum class Operator(val sign: String, val priority: Int) {
    ADD("+", 1),
    SUB("-", 1),
    DIV("/", 2),
    MULT("*", 2),
    POW("^", 3),
    PAR_OPEN("(", 4),
    PAR_CLOSE(")", 4);

    companion object {
        fun isOperator(str: String) = values().any { it.sign == str }
        fun getPriority(str: String) = values().first { it.sign == str }.priority
    }
}
