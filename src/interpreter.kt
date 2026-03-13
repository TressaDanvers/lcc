package ch.protonmail.tdanvers.lambda

fun interpret(expression: Expression): Expression {
  var result = expression
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
  return result
}

fun unsafeBeta(expression: Expression) = beta(expression, true)

fun beta(expression: Expression, unsafe: Boolean = false): Expression = when(expression) {
  is Application -> when (expression.f) {
    is Lambda -> when(expression.x) {
      is Lambda, is Application -> Parenthetical(expression.x)
      else -> expression.x
    }.let { x ->
      expression.f.e.replace(expression.f.v, x)
    }
    is Parenthetical -> beta(Application(expression.f.e, expression.x), unsafe)
    else -> {
      val decayed = beta(expression.f, unsafe)
      if (decayed == expression.f && unsafe)
        Application(expression.f, beta(expression.x, unsafe))
      else
        Application(decayed, expression.x)
    }
  }
  is Lambda -> Lambda(expression.v, beta(expression.e, unsafe))
  is Parenthetical -> beta(expression.e, unsafe)
  else -> expression
}.let(::normalizeParentheses)

fun normalizeParentheses(expression: Expression): Expression = when(expression) {
  is Value, is EmptyExpression -> expression
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
  is Lambda -> Lambda(expression.v, normalizeParentheses(expression.e))
  is Parenthetical -> normalizeParentheses(expression.e)
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
