package ch.protonmail.tdanvers.lambda

fun interpret(expression: Expression): Expression {
  var result = stripParentheses(expression)
  var decayed = beta(result)
  try {
    while (result != decayed) {
      result = decayed
      decayed = beta(result)
    }
    for (i in 0..<100) {
      result = decayed
      decayed = unsafeBeta(result)
      if (result == decayed) break
    }
  } catch (_: StackOverflowError) {
    error("overflow caused by unbounded β-decay chain")
  }
  return normalizeParentheses(result)
}

fun unsafeBeta(expression: Expression) = beta(expression, true)

fun beta(expression: Expression, unsafe: Boolean = false): Expression = when(expression) {
  is Application -> when (expression.f) {
    is Lambda ->
      expression.f.e.replace(expression.f.v, expression.x)
    else -> {
      val decayed = beta(expression.f, unsafe)
      if (decayed == expression.f && unsafe)
        Application(expression.f, beta(expression.x, unsafe))
      else
        Application(decayed, expression.x)
    }
  }
  is Lambda -> Lambda(expression.v, beta(expression.e, unsafe))
  else -> expression
}

fun stripParentheses(expression: Expression): Expression = when(expression) {
  is Value, is EmptyExpression -> expression
  is Parenthetical -> stripParentheses(expression.e)
  is Lambda -> Lambda(expression.v, stripParentheses(expression.e))
  is Application -> Application(stripParentheses(expression.f), stripParentheses(expression.x))
}

fun normalizeParentheses(expression: Expression): Expression = when(expression) {
  is Value, is EmptyExpression -> expression
  is Parenthetical -> normalizeParentheses(expression.e)
  is Lambda -> Lambda(expression.v, normalizeParentheses(expression.e))
  is Application -> {
    val left = normalizeParentheses(expression.f)
    val right = normalizeParentheses(expression.x)
    when (right) {
      is Application, is Lambda -> Parenthetical(right)
      else -> right
    }.let { right -> when (left) {
      is Lambda -> Parenthetical(left)
      else -> left
    }.let { left ->
      Application(left, right)
    } }
  }
}

fun Expression.replace(value: Value, expression: Expression): Expression = when(this) {
  value -> expression
  is Parenthetical -> Parenthetical(e.replace(value, expression))
  is Application -> Application(f.replace(value, expression), x.replace(value, expression))
  is Lambda -> alpha(Lambda(value, expression)).let { (v, e) -> Lambda(v, e.replace(value, expression)) }
  else -> this
}

fun Lambda.alpha(expression: Expression = EmptyExpression): Lambda =
  Application(this, expression).getAnyFree()
    .let { Lambda(it, e.replace(v, it)) }

fun Expression.getAnyFree(): Value {
  var free: Value = Symbol('a')
  val allValues = getAllValues()
  while (free in allValues) free++
  return free
}

fun Expression.getAllValues(): Set<Value> = when(this) {
  is Value -> setOf(this)
  is Parenthetical -> e.getAllValues()
  is Lambda -> (v.getAllValues() + e.getAllValues()).toSet()
  is Application -> (f.getAllValues() + x.getAllValues()).toSet()
  is EmptyExpression -> emptySet()
}.distinct().toSet()

operator fun Value.inc(): Value = when(this) {
  is Symbol ->
    if (c !in 'a'..<'z') Prime(Symbol('a'))
    else Symbol(c.inc())
  is Prime -> Prime(v.inc())
}
