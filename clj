#!/bin/bash
# Runs Clojure using the classpath specified in the `.clojure` file of the
# current directory.
#
# Mark Reid <http://mark.reid.name>
# CREATED: 2009-03-29
JAVA=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/bin/java 
# resolve links - $0 may be a softlink
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
CLJ_DIR=`dirname "$PRG"`
CLOJURE=$CLJ_DIR/clojure/clojure.jar
CONTRIB=$CLJ_DIR/clojure-contrib/clojure-contrib.jar
JLINE=$CLJ_DIR/jline/jline.jar
CP=$PWD:$CLOJURE:$JLINE:$CONTRIB

# Add extra jars as specified by `.clojure` file
if [ -f .clojure ]
then
	CP=$CP:`cat .clojure`
fi

if [ -z "$1" ]; then 
	$JAVA -server -cp "$CP" \
	    jline.ConsoleRunner clojure.lang.Repl    
else
	scriptname=$1
	$JAVA -server -cp "$CP" clojure.lang.Script $scriptname -- $*
fi
