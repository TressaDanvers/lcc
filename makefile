.PHONY: build test install clean

build: out/lci

test: out/test-results

install: out/lci
	mkdir -p ~/.local/bin
	cp out/lcc out/lci ~/.local/bin/

clean:
	rm -rf ./out

out/test-results: out/lci test/
	bash test/runAllTests.sh

out/lcc: src
	mkdir -p out
	sh src/generateEmbeddedInterpreter.sh
	kotlinc src/*.kt out/embeddedInterpreter.kt -include-runtime -Werror -d out/lcc.jar
	(echo '#!/usr/bin/env sh'; echo 'exec java -jar $$0 "$$@"'; cat out/lcc.jar) > out/lcc
	rm out/lcc.jar
	rm out/embeddedInterpreter.kt
	chmod +x out/lcc

out/lci: out/lcc
	out/lcc src/lci.lc -o out/lci
