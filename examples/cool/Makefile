CLASSPATH:=.
TYRUBA := java -classpath $(HOME)/Library/Jars/activation.jar:$(HOME)/Library/Jars/mail.jar:../../dist/tyruba.jar tyRuBa.applications.CommandLine

test:	BoundedStack.class Test.class
	java -classpath $(CLASSPATH) Test
%.class: %.java
	javac -classpath $(CLASSPATH) $<
%.java: %.jrub
	$(TYRUBA) -o $@ $<
clean:
	rm -f BoundedStack.java *.class
