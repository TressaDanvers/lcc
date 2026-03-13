#!/usr/bin/env bash
OUT=out/test-results

out/lcc --version > "$OUT"

run_test() {
  OUTPUT="$(out/lci "$(cat $1)")"
  EXPECTED="$(cat $1.a)"
  if [ "$OUTPUT" == "$EXPECTED" ]; then
    echo "✔️  $1 passed"
  else
    echo "❌ $1 failed - expected:$EXPECTED, got:$OUTPUT"
  fi
}

TEST_FAILS="$(run_test test/fails)"
TEST_PASSES="$(run_test test/passes)"

if ! [ "$TEST_FAILS" == "❌ test/fails failed - expected:b, got:a" ]; then
  echo "❌ check test runner - test/fails passed"
  echo "❌ check test runner - test/fails passed" >> "$OUT"
elif ! [ "$TEST_PASSES" == "✔️  test/passes passed" ]; then
  echo "❌ check test runner - test/passes failed"
  echo "❌ check test runner - test/passes failed" >> "$OUT"
fi

for i in test/*.lc; do
  TEST="$(run_test $i)"
  echo "$TEST"
  echo "$TEST" >> "$OUT"
done
