# WARNING! 
# 1) to generate the about 120 000 facts for benchmarking the factbase
# you should make sure you give the tyruba java process enough heap memory.
# Around 250 MB is probably what you need as the max heap limit. Edit the 
# tyruba shell script to make this change.
# 2) Be patient, generating the 120 000 facts takes a while.

TYRUBAPATH := $(shell pwd)/../..
JARS:=$(HOME)/Library/Jars/junit.jar:$(HOME)/Library/Jars/jakarta_regexp.jar
TYRUBA := java -Xms32m -Xmx512m -classpath $(JARS):$(TYRUBAPATH)/dist/tyruba.jar tyRuBa.applications.CommandLine

benchmark:
	time $(TYRUBA) -silent benchmark.rub $* -benchmark benchmark.qry

test:	test_facts.rub queries.rub
	time $(TYRUBA)  benchmark.rub
	time $(TYRUBA)  -silent string_games.rub
#	time $(TYRUBA)  -old_factbase benchmark.rub

queries.rub: factgen.rub
	$(TYRUBA)  -silent factgen.rub -parse ":- querygen()" . > queries.rub

#	time $(TYRUBA)  -nofactbase benchmark.rub

test_facts.rub:	factgen.rub
	$(TYRUBA) -silent factgen.rub -parse ":- factgen()" . > test_facts.rub

clean:
	rm -f test_facts.rub
	rm -f queries.rub
	rm -fr fdb
