package ch.protonmail.tdanvers.lambda

const val VALID_LAMBDA = "λ\\"
const val VALID_DOT = "."
const val VALID_OPAR = "("
const val VALID_CPAR = ")"
const val VALID_PRIME = "'"
fun Char.isValidSymbol() = isLetterOrDigit()

fun parse(source: Sequence<Char>) =
  parseExpression(source.filterNot { it.isWhitespace() }.memoize())
    .let { (expression, remaining) ->
      val x = remaining.firstOrNull()
      if (x != null)
        fatalError("unable to parse all tokens in expression, starting at ‘$x’")
      expression
    }

fun parseExpression(source: Sequence<Char>): Pair<Expression,Sequence<Char>> =
  source.splitFirst()
    ?.let { (c, cs) ->
      parseLambda(c, cs) ?:
      parseParenthetical(c, cs) ?:
      parseValue(Symbol(c), cs)
    }?.let { (e, cs) ->
      parseExpression(cs)
        .takeIf { (it) -> it !is EmptyExpression }
        ?.let { (eb, cs) -> balance(Application(e, eb)) to cs }
        ?: (e to cs)
    }
    ?: (EmptyExpression to source)

fun balance(application: Application): Application {
  var appl = application
  while (appl.x is Application)
    appl = Application(Application(appl.f, appl.x.f), appl.x.x)
  if (appl.f is Application)
    appl = Application(balance(appl.f), appl.x)
  return appl
}

fun parseLambda(lambdaSymbol: Char, source: Sequence<Char>): Pair<Expression,Sequence<Char>>? =
  lambdaSymbol.takeIf { it in VALID_LAMBDA }
    ?.let { source.splitFirst() }
    ?.let { (c, cs) -> parseValue(Symbol(c), cs) }
    ?.let { (v, cs) -> parseDotExpression(cs)?.let { (e, cs) -> Lambda(v, e) to cs } }

fun parseParenthetical(opener: Char, source: Sequence<Char>): Pair<Expression,Sequence<Char>>? =
  opener.takeIf { it in VALID_OPAR }
    ?.let { parseExpression(source) }
    ?.let { (e, cs) -> cs.splitFirst()?.let { e to it } }
    ?.takeIf { (_, ccs) -> ccs.first == VALID_CPAR[VALID_OPAR.indexOf(opener)] }
    ?.let { (e, ccs) -> Parenthetical(e) to ccs.second }

fun parseDotExpression(source: Sequence<Char>): Pair<Expression,Sequence<Char>>? =
  source.splitFirst()
    ?.let { (c, cs) -> c.takeIf { it in VALID_DOT }?.let { cs } }
    ?.let { cs -> parseExpression(cs) }

fun parseValue(value: Value, source: Sequence<Char>): Pair<Value,Sequence<Char>>? =
  value.takeIf { it is Prime || (it as Symbol).c.isValidSymbol() }?.let {
    source.splitFirst()
      ?.let { (c, cs) -> when {
        c in VALID_PRIME -> parseValue(Prime(value), cs)
        else -> value to source
      } }
      ?: value to source
  }
