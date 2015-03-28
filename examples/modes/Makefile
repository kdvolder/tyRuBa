TYRUBAPATH := $(shell pwd)/../..
CLASSPATH := $(HOME)/Library/Jars/activation.jar:$(HOME)/Library/Jars/mail.jar:$(TYRUBAPATH)/dist/tyruba.jar
TYRUBA := java -classpath $(CLASSPATH) tyRuBa.applications.CommandLine -silent \
	-classpath $(TYRUBAPATH)

all:  
	time $(TYRUBA) ast.jrub
	javac *.java

clean:
	rm -f *.class *.java *\~ # all .java are generate in this dir
