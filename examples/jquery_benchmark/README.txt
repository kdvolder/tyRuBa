How to benchmark TyRuBa running on a database produced from JQuery.

Step 1) Create a database:

  a) Start clean
  
  Before creating a database you may want to clean out the JQuery .metadata so
  it will be easy to find which one is actually your new DB.
  
  % cd $(WORKSPACE)/.metadata/.plugins
  % rm -fr ca.ubc.jquery
  
  You should also make sure to upgrade to a compatible version of JQuery. Such a
  version should ship with small .jar file called "sourceloc.jar" which you will need
  in order to make tyruba able to read the JQuery database offline.
  
  b) Start Eclipse and parse the code you want.
  
  Create a workingset of the sources you want to parse. The sources must all
  reside within a single project.
  
  When this done, quit Eclipse and let JQuery properly save the database to disk.
  
  c) Archive your database in a tar or zip file
  
  The database is found under a numbered directory inside ca.ubc.jquery/metadata.
  
  If you started clean there should be only one and its number should be 100 (if you 
  create more, each additional one will be numbered 101, 102 etc.)
  
  % cd $(WORKSPACE)/.metadata/.plugins/ca.ubc.jquery
  % tar cf benchdata.tar 100
  
  d) setup tyruba to run in "JQuery compatible mode".
  
  You will require to set up TyRuBa as usual but also make sure you add the following
  sourceloc.jar to the TyRuBa classpath, it is found in your installed JQuery pluggin or
  source distribution.
  
  $(ECLIPSE)/plugins/ca.ubc.jquery/sourceloc.jar
  
  For example, my tyruba shell script contains the following:
  
  #!/bin/sh
  System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home/bin/java -Xms32m -Xmx512m \
      -classpath $HOME/Desktop/eclipse/workspace/ca.ubc.jquery/dist/sourceloc.jar:$HOME/Desktop/eclipse/workspace/ca\
.ubc.jquery/lib/jakarta-regexp-1.2.jar:$HOME/Desktop/eclipse/plugins/org.junit_3.8.1/junit.jar:$HOM\
E/tyruba/dist/tyruba.jar tyRuBa.applications.CommandLine $*
  
  e) Create a working directory where you set up a "simulated" JQuery environment.
  
  It should look like this:
  
  workingdir/
     benchdata_tyruba_src.tar    an archived JQuery database
     rules/		This is a copy of $(JQUERY_PLUGIN)/rules/
     user.rub    A copy of the user.rub file found in JQuery's metadata directory.
     100/ 		A captured benchmark database (in expanded form)
     run			a script to run the benchmarks in this example. (run setup first to
                 unarchive the example database.
     setup		a script that untar's the test database archive for use.
  
  You can give the 1xx directories more meaningful names if you like.
  
  DO NOT DELETE the respective .tar files. You may need them again if the database 
  gets corrupted somehow.
  
  Now you should be able to run tyruba from the commandline for example:
  
  cd workingdir/
  tyruba -dbdir 100 rules/initfile.rub user.rub -i
  
  Will start an interactive tyruba engine after loading the initfile.rub and initializing
  properly to work with the 100 database. This gives you an environment similar to what
  you would be getting when running inside of JQuery.
  
  You can now type queries (start with a ":-" and end 
  with a ".".
  
  End your session by sending an EOF file character (CTRL D on most unix systems).
  
  Note: If TyRuBa crashes without a proper save of the database then the DB will corrupted
  and automatically deleted on the next run, so keep those .tar files around just in case.
  
  To run a series of queries as a benchmark file create a .qry file which contains exactly 
  one query expression (without a leading ":-" or trailing "." per line of text.
  
  Then invoke tyruba from the commandline using the -benchmark option. For example:
  
    tyruba -dbdir 100 rules/initfile.rub user.rub -benchmark mybenchmark.qry
