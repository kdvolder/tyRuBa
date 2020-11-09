TyRuBa
======

TyRuba's former home is here http://tyruba.sourceforge.net/ documentation
for the language still lives there for now.  There are some more documentation 
files there, describing the actual logic language.

##NEW IN RELEASE 8.0.0

  Removed 'strict' typing. The concept seemed broken and annoying. Predicates that 'expect'
  certain types of values will no simply 'fail' in the usual way when passed values of
  the 'wrong type'. I.e. the predicates are simply considered to be 'false' on these
  values and they are filtered from the query result.

##NEW IN RELEASE 7.4.1

  TyRuBa now provides a jar that contains all of its dependencies for a somewhat 
  easier 'download and run' experience. 

##NEW IN RELEASE 7.4

  Essentially, this is just the 7.3 release with few changes to 
  remove the cobwebs from a code base that hasn't been looked at in years.
  The main goal for this release was to get things setup with a proper
  CI process resulting in builds properly publishing artefacts to maven central. 

##NEW IN RELEASE 7.3

  Bugfixes, in particular one bugfix having to do with TyRuBa very inconsiderately
  closing System.out or System.err when its done. This really didn't seem to
  cause any trouble for me (Mac OS X) since closing them seems to be be a null
  operation. However, apparantly it may cause some problems for people on other
  platforms who may experience trouble using those streams afterwards. 

##NEW IN RELEASE 7.2 (internal release only)

  Commandline options: -benchmark -cachesize -dbdir
  
  Java constructors allow this: "foo(1,2,3)"::ca.ubc.jquery.ast.SourceLocation
  
  Background page cleaning.
  
  Bug fixes.

##NEW IN RELEASE 7.1

  Lots' of optimizations, refactorings, bug fixes has been going on since release
  6.1. The backend was basically completely reimplemented.

##NEW IN RELEASE 6.1  

  (note release 6.0 was never made public because of too many problems with the
  new disk-based factbase implementation)

  This release is a completely revised version of TyRuBa. The query language
  has been considerably changed in order to add a type and mode system. As 
  a result, most of the old changelog and readme file has become 
  obsolote and has been moved to "old_README".

  For detailed explanation about the TyRuBa language, in particular the
  newly added type and mode system see the
  online documentation (language reference manual + tutorial) provided as 
  .html files in this distribution package and online at tyruba.sourceforge.net.

  For old tyruba users, unfortunaltely your old code will probably need
  considerable work to make it work in this new version. If you need help with this
  feel free to send me email at kdvolder@cs.ubc.ca.

  New features in this version:
 
    - complete revision of the query language
    - Static type checking and mode checking.
    - A disk based representation of factbases
       => allows bigger factbases to be used with less memory at the cost
          of running slower.
          
       Factbases can be made persistent between runs of the query engine.
        (The API for doing this right now is not yet clearly doumented or stable)

##BUILDING:

With ant:

A simple ant build is defined in 'build.xml' and can be run easily from
Eclipse with the following steps:

 - Download [javacc-4.0](https://java.net/projects/javacc/downloads/download/oldversions/javacc-4.0.zip) 
   and unzip it somewhere
 - Edit build.xml and update JavaCC references to point to where you copied it.
 - Right-click build.xml and select "Run As >> Ant Build"
 - Find the compiled jar at `build/tyruba.jar`

##RUNNING:

To run the command-line application:

  1) Put the tyruba.jar file on your classpath. 
  2) Start the main class: tyRuBa.applications.CommandLine

The most convenient way to accomplish this and run tyruba from the 
commandline is probably to have a look at the bin/tyruba shell script. 
Move this to somewhere on your path. You may need to edit it so that it 
points to wherever you stored your tyruba.jar file.

If you did all that, you should be able to run tyruba from the commandline
by invoking the tyruba command. See below for a description of commandline
options and syntax.

##COMMANDLINE INTERFACE

"tyruba" ["-noinit"] ("-silent" | "-nocache" | "-i" | "-o" filename | filename)*

This will start the tyruba logic engine.  

##INITFILES

Before starting to process the commandline, tyruba starts by reading a file
called initfile.rub.  This file is packaged with tyruba in its jar file. 
You can suppress loading this file by having the -noinit option as the very
FIRST option of the commandline.  Note that a copy of the initfile lives in
lib/.  If you edit them you must rebuild the .jar file or your changes
won't be taking effect.  There is documentation on some of the basic
predicates at http://tyruba.sourceforge.net.

##COMMANDLINE PROCESSING

After reading the initfile, the commandline is read from left to
right. Options and filenames are processed directly, in that order
(i.e.  left to right).

The engine will load logic files, parse them and interpret them.
Queries occurring in the parsed files will be processed directly after
they are parsed. The result of the query will be printed on the
standard error device.  Queries are normally not part of a regular
tyruba program, but they may be helpful as a rudimentary debugging
mechanism to inspect the state of the logic database, when the program
performs unexpectedly.  

For more information about using tyruba, how to write logic rules and
facts etc. Go see the TyRuBa tutorial/manual file. (Manual is not
complete at this time)

##COMMANDLINE OPTIONS

 -noinit 

     This option must be the very first in the command-line! It
     suppresses reading of the standard initfile "initfile.rub" which is
     packaged in the .jar file.  Use this option if you want to use a
     different initfile. Then load your own initfile just like any
     ordinary file. Note if you want to use this option, you may find
     it helpfull to browse the initfiles in lib/ (for inspiration on 
     writing your own)

-reconnect

     This tells TyRuBa to retain the contents of persisted fact bases.
 
 -o <filename>

     Sets the output file to the given filename. What is written to
     the output file is only the output generated as a side effect
     of evaluating a call to the "write_output" predicate (described
     in the tyruba library reference documentation). 
     
     **Results of queries are written to standard error
     **device and are not affected by this option.

     Note also that due to the way that options and filenames are
     interpreted left-to-right, this option only affects the output
     generated by input files occurring to the right of this option! 

     You can use multiple -o options in a single command line.  Thus
     different output files can be written in a single run of the tyruba
     program.  This will probably be considerably faster than relaunching
     tyruba, especially if the generation of several of the files share a
     lot of computation. (This is because of tyruba's caching mechanism).

  -i 

     Start processing input from System.in (standard input) instead of from
     a file.  A user can now type queries and directives "interactively". 
     This is a very rough interactive mode.  When entering queries this
     will produce output on standard error. When entering #generate 
     directives output will be printed on the current output stream as set 
     by the -o option.

  -silent

     Supresses the printing of the ".", "o" and "H" characters which indicate
     query engine processing. (Note: remember left to right interpretation of
     commandline options. The option will take effect only with respect to
     commandline arguments to the right of it) 

  -nocache

     Turns off tyruba's query cache. This will considerably slow down tyruba 
     but saves memory. Note that this also changes the way TyRuBa works to
     be more like prolog (i.e. no more tabling and no more removal of duplicate elements
     in query results, since these are functions performed by the query cache).
     
     ALMOST CERTAINLY, you do not want to turn the query cache of, it changes the
     semantics of the language (as described above) and has a big impact on speed
     as well. Likely this option will be removed in future versions.

  -parse <query-rule-or-fact string>

     Example tyruba -classpath myclasses ast.jrub -parse ":- genfiles ."
     
  -dbdir <path-to-dirctory>
  
     Use the given path to store the database. If there is a database there already
     its facts will be loaded.
     
  -cachesize <number>
  
     Set the tyruba node cache size. Default is 5000. More nodes means more of the
     database is kept in memory.

  -bgpager

     Enable the creation of a backgroun page cleaing thread. 

     This thread writes dirty database index nodes to disk in
     background, whenever the foreground pager is idle for a while.

     This is disabled by default in CommandLine mode since it
     has the potential to slow down query processing by taking
     away processor time from actual query processing. The
     bgpager is really intended for an interative GUI application 
     that uses the "fastBackup" method.
     
  -benchmark <file>
  
     The file is read and every line should contain a single query to execute. 
     Each query is executed, all the results from the query are retrieved and
     counted and the time taken to do this is reported.
     
     Comments can be included in the file as lines that start with "//" (with no
     leading spaces!).
     
     The special comment "//SCENARIO" anywhere in the file will make the benchmark
     run in "scenario" mode. This is intended for running long series of queries
     that were collected (for example) by enabling the QueryLogger in JQuery.
     
     The normal (non-scenario) mode assumes that a benchmark contains a small number of
     long running tests to be run one by one. The clock is started and stopped in between 
     tests and progress information is printed while running the tests. The garbage
     collector is invoked in between test to start each test with a clean gc state.
     
     In scenario mode all tests are run at once, without stopping the clock or
     reporting progress or forcing gc in between the tests.
     Results are recorded and reported at the end of the entire testrun.

##INCLUDE FILES

Any file loaded by tyruba may contain #include directives. They look like

      #include "a filename"

The filename is interpreted relative to the file in which the include
directive is encountered. #include directives in -i mode are not
supported currently.

##TYRUBA WHILE RUNNING

Tyruba will print some strange characters while running. There are four
different ones: ".", "o", "H" and "@". What the heck is that? :-)

You probably don't need to know. Just look at them as a sign that
tyruba is working on a query. (I've sometimes told my students to think 
of it as "logic sweat" :-). But since you asked... Tyruba has a two
level cache/optimization of the main rulebase to speed up
queries. This is quite necessary because without it tyruba is immensely
slow.

I started building tyruba and did not care about speed... and so I
just represented a rule base as a recursive linked list type of
structure. Of course this got me into trouble soon, with programs
that generate two pages of Java text but run for 45 minutes to do so.

The rulebase is still a simple unorganized list (it is now a vector,
for another reason: Java does not handle tail recursion very nicely,
so I had to get rid of the recursion). Three optimization mechanisms now
are built into tyruba. 

The first one is an "index" of the rulebase. The "index-key" is the
query as it appears in a rule, or as it is typed by the user. It is
assumed that several queries may be launched that have this same
pattern, but possibly with different bindings for the variables. To
speed up all such queries, the first such query that gets launched
will cause tyruba to build a specialized copy of the rule base by
unifying each fact and rule in the rulebase with that query's original
form (i.e. without the binding for the variables it may have
acquired). This typically reduces the number of rules as it will only
yield rules which actually match the query and no other rules. So, the
first query will be slow but all subsequent queries of the same form
will be much faster because immediately it finds the right rules in
the index.

A second optimization caches the exact result of a query. That means
that if a query of exactly the same form is encountered twice, it will
reuse the answers computed the first time. The second query will be
"instantaneous". This optimization is quite important in the way tyruba
is often used as a code generation tool. Very often a lot of rules
that generate bits of code have the same condition attached to
them. Thus, that query will be run many, many times. Often this query
is quite computationally intensive.

Actually, this optimization is a bit more complex in reality
because the cached query results are computed lazily. They are stored
in the cache *before* they are actually computed. So sometimes a query
may actually refer to its "precached" result in computing its own
result. This mechanism is quite similar to something called "tabling"
as found for example in the XSB logic system.  This mechanism allows
tyruba to detect certain kinds of recursive loops and answer them
correctly, where an ordinary prolog program would loop infinitely.

The third optimization is a separate, indexed data structure to store
ground facts. Since release 6.0 this structure is partially stored on
disk when it gets very big. Ground facts are facts which don't have any free
variables. The facts are stored/accessed by means of an indexing
schema making it much more efficient if you have a lot of facts.

Now, you also understand what the three strange characters mean. Every
time a query is launched, including ones called indirectly through
rules a character is printed.

A "." means "this is a fresh query. I have never seen anything like
it I must build a specialized rule base and put it in the rule-index. This
is the slowest kind of query.

A "o" means "I don't have that exact same query in my cache, but a
query almost like this one is in my rulebase-index so I can get the
right rules very fast, however then I still have to compute the result
using those rules. This is the second best with respect to speed.

A "H" means "A HIT in the cache, I've seen that query before and I
already know the result".

Entries in the cache are represented using so-called "soft"
references. This means that the Java garbage collector can reclaim
them when memory is running low. This is not a real problem, because
they can always be recomputed. So this trades memory for speed. To
have some kind of indication of the impact of entry reclamation by the
garbage collector, a "@" will be printed when a query touches a
cleared cache entry. 

There is a fifth character you may occasionally see: a "#". This is
printed when, while computing a lazy query result, tyruba finds a
circular dependency, i.e. result that depends on itself. Such "bad
loops" are cut off by the algorithm and tyruba then tries another way
to find results. If you encounter this, it may be wise to try and get
rid of it, i.e. figure out where the loop is and try and write your
program so it goes away. Usually it works ok, with the # but I suspect
that in rare occasions you may lose results which you'd actually
expect. 

Typically, when tyruba starts you will see a lot of "." then more and
more "o" and "H" will appear and execution speeds up.

There are cases where tyruba cleans out the caches for
consistency. The mechanism does not do anything sophisticated about
managing cache consistency. As soon as you add more facts and rules to
the rulebase, the cache and index is cleared completely. Clearly not
the most efficient way to implement this, but definitely the easiest
:-). I did not spend much effort on this because 1) it is quite tricky
to implement more sophisticated strategies. So far, the need for more
sophisticated cache management has not been felt as something worht 
spending the effort. 

##CREDITS 

 1) Third party source code:
  
  The regexp package from apache was used to implement tyruba's regexp
  matching utility. See http://www.apache.org for details.
 
  The source code for the regexp package is included with the tyruba
  distribution package. Copyright information for this portion of the
  code can be found in the actual source file comments.
  
 2) Authors:
 
   Over time, multiple people have been involved with working on
   all or part of TyRuBa. The following people have been instrumental
   in making TyRuBa what it is today:
   
   Kris De Volder (That's me :-)
      Built the first version of TyRuBa and maintained it singlehandedly
      up to version 5.9. Continues being the principal maintainer
      of TyRuBa's code base till present.
      
   Terry Hon   (summer student intern 2003)
      Responsible for working out many of the details of the type and
      mode system and implementing them in TyRuBa 6.0
      
   Chris Burns (summer student intern 2003)
      Responsible for the construction of TyRuBa's partially disk-based 
      representation of the factbase.

   Jim Riecken (summer student intern 2004)
     Reimplementation of the disk based implementation. Now works much 
     faster and better.
    
--------------------------------------------------------------------------------

I hope you enjoy tyruba. If you like tyruba send me a
message. If you don't like it, send me a message as well! Every bit of
information is useful in making it better.

Comments, questions, bug reports feature requests -> visit the tyruba
project page on github.

Or... send me email at kris.de.volder@gmail.com
