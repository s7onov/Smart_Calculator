package calculator

import java.math.BigInteger
import java.util.*

class Calculator {
    object Const {
        const val HELP_STRING = """
        The program calculates the expression of numbers. Addition and subtraction are supported.
        If you input -- it would be read as +; if you input ----, it would be read as ++.
        
        Version 8 
        BigInteger are now supported
        
        Version 7 
        Operations *, /, ^ and brackets () are now supported
        
        Version 6 
        The program now supports variables.
        Go by the following rules for variables:
        We suppose that the name of a variable (identifier) can contain only Latin letters.
        A variable can have a name consisting of more than one letter.
        The case is also important; for example, n is not the same as N.
        The value can be an integer number or a value of another variable.
        It is possible to set a new value to an existing variable.
        To print the value of a variable you should just type its name.
        """
        const val INVALID_EXPRESSION = "Invalid expression"
        const val UNKNOWN_COMMAND = "Unknown command"
        const val INVALID_IDENTIFIER = "Invalid identifier"
        const val UNKNOWN_VARIABLE = "Unknown variable"
        const val INVALID_ASSIGNMENT = "Invalid assignment"
        val VAR_REGEX = "[a-zA-Z]+".toRegex()
        val START_WITH_LETTER_REGEX = "[a-zA-Z]+.*".toRegex()
    }

    open class Entity(val isOperator: Boolean)
    enum class Operations(val priority: Int) {
        ADD(1),
        SUB(1),
        MULTIPLY(2),
        DIV(2),
        POW(3),
        LEFT_BRACKET(0),
        RIGHT_BRACKET(0),
    }
    data class Operator(val operation: Operations): Entity(true)
    data class Value(val number: BigInteger): Entity(false)

    private val memory = mutableMapOf<String, BigInteger>()
    private val list = mutableListOf<Entity>()
    private val stack = Stack<Operator>()

    fun start() {
        while (true) {
            val input = readln()
            if (input.isEmpty()) continue
            else if (input.startsWith("/")) {
                if (input == "/exit") break
                else if (input == "/help") println(Const.HELP_STRING.trimIndent())
                else println(Const.UNKNOWN_COMMAND)
            } else {
                val result = processInput(input)
                if (result.isNotBlank()) println(result)
            }
        }
        println("Bye!")
    }

    fun processInput(input: String): String {
        return when {
            input.matches(".*=.*".toRegex()) -> processAssignment(input)
            else -> processExpression(input)
        }
    }

    private fun processAssignment(input: String): String {
        val left = input.substringBefore("=").trim()
        val right = input.substringAfter("=").trim()
        if (!left.matches(Const.VAR_REGEX)) return Const.INVALID_IDENTIFIER
        when (val result = parseOrGetFromMemory(right, Const.INVALID_ASSIGNMENT)) {
            is String -> return result
            is BigInteger -> memory[left] = result
        }
        return ""
    }

    private fun parseOrGetFromMemory(string: String, parsingError: String): Any {
        if (string.matches(Const.START_WITH_LETTER_REGEX)) {
            if (!string.matches(Const.VAR_REGEX)) return Const.INVALID_IDENTIFIER
            if (!memory.contains(string)) return Const.UNKNOWN_VARIABLE
            return memory[string]!!
        } else {
            return try {
                string.toBigInteger()
            } catch (e: Exception) {
                parsingError
            }
        }
    }

    private fun processExpression(input: String): String {
        list.clear()
        stack.clear()
        val array = input
            .replace("\\s+\\s", " + ")
            .replace("\\s-\\s", " - ")
            .replace("\\s*\\s", " * ")
            .replace("\\s/\\s", " / ")
            .replace("\\s^\\s", " ^ ")
            .replace("(", " ( ")
            .replace(")", " ) ")
            .split(" ")
        for (it in array) {
            if (it.isBlank()) continue
            when {
                it.matches("-+".toRegex()) -> {
                    if (it.length % 2 == 1) toPostfix(Operations.SUB)
                    else toPostfix(Operations.ADD)
                }
                it.matches("[+]+".toRegex()) -> toPostfix(Operations.ADD)
                it == "*" -> toPostfix(Operations.MULTIPLY)
                it == "/" -> toPostfix(Operations.DIV)
                it == "^" -> toPostfix(Operations.POW)
                it == "(" -> toPostfix(Operations.LEFT_BRACKET)
                it == ")" -> if (!toPostfix(Operations.RIGHT_BRACKET)) return Const.INVALID_EXPRESSION
                else -> {
                    // If the scanned element is an
                    // operand, add it to output.
                    when (val result = parseOrGetFromMemory(it, Const.INVALID_EXPRESSION)) {
                        is String -> return result
                        is BigInteger -> list += Value(result)
                    }
                }
            }
        }
        // pop all the operators from the stack
        while (!stack.isEmpty()) {
            if (stack.peek().operation == Operations.LEFT_BRACKET)
                return Const.INVALID_EXPRESSION
            list += stack.peek()
            stack.pop()
        }

        return evaluatePostfix(list)
    }

    private fun toPostfix(op: Operations): Boolean {
        // If there is a LEFT_BRACKET,
        // push it to the stack.
        if (op == Operations.LEFT_BRACKET)
            stack.push(Operator(op))

        // If there is a RIGHT_BRACKET,
        // pop and output from the stack
        // until an LEFT_BRACKET is encountered.
        else if (op == Operations.RIGHT_BRACKET) {
            while (!stack.isEmpty()
                && stack.peek().operation != Operations.LEFT_BRACKET) {
                list += stack.peek()
                stack.pop()
            }

            if (stack.isEmpty()) return false
            else stack.pop()
        }
        else // an operator is encountered
        {
            while (!stack.isEmpty()
                && op.priority <= stack.peek().operation.priority) {

                list += stack.peek()
                stack.pop()
            }
            stack.push(Operator(op))
        }
        return true
    }

    // Method to evaluate value of a postfix expression
    private fun evaluatePostfix(exp: MutableList<Entity>): String {
        // create a stack
        val stack = ArrayDeque<Value>()

        // Scan all the data
        for (element in exp) {

            if (element.isOperator) {
                element as Operator
                val val1 = stack.pop()
                val val2 = if (!stack.isEmpty()) stack.pop() else Value(BigInteger.ZERO)
                stack.push(execute(val2 as Value, val1 as Value, element.operation))

            } else {
                // If the scanned element is an operand (number here),
                // push it to the stack.
                stack.push(element as Value)
            }
        }
        return stack.pop().number.toString()
    }

    private fun execute(valueA: Value, valueB: Value, operation: Operations): Value {
        return when (operation) {
            Operations.ADD -> Value(valueA.number + valueB.number)
            Operations.SUB -> Value(valueA.number - valueB.number)
            Operations.MULTIPLY -> Value(valueA.number * valueB.number)
            Operations.DIV -> Value(valueA.number / valueB.number)
            Operations.POW -> Value(valueA.number.pow(valueB.number.toInt()))
            else -> throw Exception("Unsupported operation")
        }
    }
}

fun main() {
    Calculator().start()
}
