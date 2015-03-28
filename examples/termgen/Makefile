TYRUBAPATH := $(shell pwd)/../..
CLASSPATH := $(TYRUBAPATH)/src
#CLASSPATH := .

MEM :=  -Xms32m -Xmx32m
#PROF := -Xrunhprof:cpu=times #,depth=1
PROF := # -Xrunhprof:cpu=samples,depth=20,heap=all

TYRUBA := tyruba
#java $(MEM) $(PROF) -classpath $(TYRUBAPATH)/tyruba.jar tyRuBa.applications.CommandLine -classpath $(CLASSPATH) -silent

#-Xrunhprof:cpu=times -silent

all:
	#cd $(TYRUBAPATH) ; make all
	time $(TYRUBA) -silent -classpath $(CLASSPATH) *.jrub -parse :- genfiles .
	#time $(TYRUBA) *.jrub -parse :- genfiles .
	#javac -classpath $(CLASSPATH) terms/*.java

tyruba_terms:
	#cd $(TYRUBAPATH) ; make all
	time $(TYRUBA) tyruba_terms.rub -parse :- genfiles .
	javac -d $(TYRUBAPATH)/classes -classpath $(CLASSPATH) terms/*.java

clean:
	rm -f terms/* *.java *\~ 
