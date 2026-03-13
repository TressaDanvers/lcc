package ch.protonmail.tdanvers.lambda

fun interpret(expression: Expression): Expression {
  var result = expression
  var decayed = beta(result)
  while (result != decayed) {
    result = decayed
    decayed = beta(result)
    while (decayed is Parenthetical)
      decayed = decayed.e
  }
  return result
}

fun beta(expression: Expression): Expression = when(expression) {
  is Lambda -> Lambda(expression.v, beta(expression.e))
  is Application -> when(expression.f) {
    is Application -> Application(beta(expression.f), expression.x)
    is Lambda -> expression.f.e.replace(expression.f.v, expression.x)
    is EmptyExpression -> expression.x
    is Parenthetical -> when(expression.f.e) {
      is Parenthetical, is Lambda -> beta(Application(expression.f.e, expression.x))
      is EmptyExpression -> expression.x
      else -> expression
    }
    else -> expression
  }
  is Parenthetical -> when(expression.e) {
    is Parenthetical -> beta(expression.e)
    else -> Parenthetical(beta(expression.e))
  }
  else -> expression
}

fun Expression.replace(value: Value, expression: Expression): Expression = when(this) {
  value -> expression
  is Parenthetical -> Parenthetical(e.replace(value, expression))
  is Application -> Application(f.replace(value, expression), x.replace(value, expression))
  is Lambda -> mask(expression).let { (v, e) -> Lambda(v, e.replace(value, expression)) }
  else -> this
}

fun Lambda.mask(expression: Expression = EmptyExpression): Lambda =
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
