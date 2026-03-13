package ch.protonmail.tdanvers.lambda

fun interpret(expression: Expression): Expression {
  var result = expression
  var decayed = beta(result)
  try {
    while (result != decayed) {
      result = decayed
      decayed = beta(result)
      while (decayed is Parenthetical)
        decayed = decayed.e
    }
  } catch (_: StackOverflowError) {
    error("overflow caused by unbounded β-decay chain")
  }
  return result
}

fun beta(expression: Expression): Expression = when(expression) {
  is Lambda -> when (expression.e) {
    is Parenthetical -> beta(Lambda(expression.v, expression.e.e))
    else -> Lambda(expression.v, beta(expression.e))
  }
  is Application -> when(expression.f) {
    is Lambda -> expression.f.e.replace(expression.f.v, expression.x)
    is EmptyExpression -> expression.x
    is Parenthetical -> when(expression.f.e) {
      is EmptyExpression -> beta(expression.x)
      is Lambda, is Parenthetical -> beta(Application(expression.f.e, expression.x))
      else -> Application(Parenthetical(beta(expression.f.e)), expression.x)
    }
    is Application -> {
      val fDecay = beta(expression.f)
      if (fDecay != expression.f)
        Application(fDecay, expression.x)
      else
        Application(expression.f, beta(expression.x))
    }
    else -> expression
  }
  is Parenthetical -> when(expression.e) {
    is Parenthetical -> beta(expression.e)
    else -> {
      val decayed = beta(expression.e)
      when (decayed) {
        is Application, is Value -> decayed
        else -> Parenthetical(decayed)
      }
    }
  }
  else -> expression
}

fun Expression.replace(value: Value, expression: Expression): Expression = when(this) {
  value -> expression
  is Parenthetical -> Parenthetical(e.replace(value, expression))
  is Application -> Application(f.replace(value, expression), x.replace(value, expression))
  is Lambda -> mask(Lambda(value, expression)).let { (v, e) -> Lambda(v, e.replace(value, expression)) }
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
