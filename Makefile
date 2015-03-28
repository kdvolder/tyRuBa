#
# Note this file is more or less obsolete and not really used/maintained.
# Use the ant build script instead.
#

cvs_release_tag := release_6_1
cvs_release_comment := "The first official beta release with type checking, mode checking and disk-based factbase"

empty:=
space:= $(empty) $(empty)
JARS:=$(HOME)/Library/Jars/junit.jar:$(HOME)/Library/Jars/regexp.jar
IKVM_HOME:=$(HOME)/localunix/ikvmbin-0.12.0.0/ikvm/bin

TYRUBAPATH := $(shell pwd)
BUILD := build

JAVAC := javac -classpath $(JARS):$(TYRUBAPATH)/src -g -d $(BUILD)
JAVA := java -classpath $(JARS):$(TYRUBAPATH)/tyruba.jar
JAR := jar
TYRUBA := java -Xms32m -Xmx64m -classpath $(JARS):$(TYRUBAPATH)/dist/tyruba.jar tyRuBa.applications.CommandLine

#ikvmc is the compiler of ikvm an implementation JVM in .NET. It compiles java bytecodes to .NET CIL
IKVMC := ikvmc -target:exe $(JARS)

TYRUBA_PACKAGES := org org/apache org/apache/regexp serp serp/util tyRuBa tyRuBa/applications tyRuBa/engine tyRuBa/engine/compilation tyRuBa/engine/factbase tyRuBa/engine/factbase/hashtable tyRuBa/engine/visitor tyRuBa/modes tyRuBa/parser tyRuBa/tdbc tyRuBa/tests tyRuBa/tests/test_space tyRuBa/util tyRuBa/util/pager

# note compilation of the mail goody requires that Java Mail API is installed 
# and the mail.jar and activation.jar files are in JARS variable declared 
# at the top of this
# makefile. If you don't have them installed you can avoid compilation errors
# by commenting out the line above. (So the mail goody will not be compiled)
# you probably also want to remove the entry in the lib/initfile.rub to
# avoid class loading errors when the initfiles are parsed.

PACKAGES := $(TYRUBA_PACKAGES)
SOURCES := $(wildcard $(patsubst %, src/%/*.java, $(PACKAGES)))
CLASSES := $(patsubst src/%.java, $(BUILD)/%.class, $(wildcard $(SOURCES)))

RUB := lib/*

all: 	src/tyRuBa/parser/TyRuBaParser.java dist/tyruba.jar

netrun:	dist/tyruba.exe
	mono dist/tyruba.exe -silent examples/benchmark/toposort.rub -i 

dist/tyruba.exe:	dist/tyruba.jar
	$(IKVMC) -out:$@ -main:tyRuBa.applications.CommandLine $<

#dist/tyruba-test.exe:	dist/tyruba.jar
#	$(IKVMC) -out:$@ -main:tyRuBa.tests.AllTests $<

src/tyRuBa/parser/TyRuBaParser.java:	src/tyRuBa/parser/TyRuBaParser.jj
	rm -f $@
	cd src/tyRuBa/parser;javacc TyRuBaParser.jj
	chmod a-w $@

$(BUILD)/%.class: src/%.java src/tyRuBa/parser/TyRuBaParser.java
	$(JAVAC) $(SOURCES)

run:	$(BUILD)/tyruba.jar
	java

dist:
	mkdir dist

dist/tyruba.jar: dist $(CLASSES) src/$(RUB)
	cd $(BUILD);$(JAR) cf ../dist/tyruba.jar *
	cd src;$(JAR) uf ../dist/tyruba.jar $(RUB)

clean:
	find . -name "*.class" -exec rm -f {} \;
	find . -name "*.backup" -exec rm -f {} \;
	find . -name "*~" -exec rm -f {} \;
	find . -name ".DS_Store" -exec rm -f {} \;
	find . -name ".nbattrs" -exec rm -f {} \;
	find . -name "filesystem.attributes" -exec rm -f {} \;
	rm -fr $(BUILD)/* 
	rm -fr dist/* 
	rm -f tyruba.jar tyRuBa/parser/ASCII_UCodeESC_CharStream.java
	rm -f src/tyRuBa/parser/ParseException.java
	rm -f src/tyRuBa/parser/Token.java
	rm -f src/tyRuBa/parser/TokenMgrError.java
	rm -f src/tyRuBa/parser/TyRuBaParser.java
	rm -f src/tyRuBa/parser/TyRuBaParserConstants.java
	rm -f src/tyRuBa/parser/TyRuBaParserTokenManager.java
	rm -fr tyruba_*
	cd examples/ast;make clean

javadoc:
	javadoc $(SOURCES) -public -d html -use

cool: all
	cd examples/cool;make clean; make

ast: all
	cd examples/ast;make clean; make

unrelease:
	cvs -q tag -d $(cvs_release_tag)
	rm -fr tyruba_$(cvs_release_tag)*

tag:
	cvs -q tag -c -F $(cvs_release_tag)
untag:
	cvs -q tag -d $(cvs_release_tag)

commit:
	cvs -q commit -m $(cvs_release_comment)
	make tag

update:
	cvs -q update

export:
	cvs -q -d :ext:kdvolder@cascade.cs.ubc.ca:/cs/spl/hop/cvs \
	   export -r $(cvs_release_tag) -d tyruba_$(cvs_release_tag) tyruba

tests:
	make cool;make unit_tests;make ast

release:
	make tag
	make export
	cd tyruba_$(cvs_release_tag);rm -fr $(NOT_FOR_RELEASE)
	tar cf tyruba_$(cvs_release_tag).tar tyruba_$(cvs_release_tag)
	cd tyruba_$(cvs_release_tag);mkdir $(BUILD) # make unit_tests
	tar cf tyruba_$(cvs_release_tag)_compiled.tar tyruba_$(cvs_release_tag)
	gzip tyruba_*.tar
