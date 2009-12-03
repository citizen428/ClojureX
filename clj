#!/bin/bash
# Runs Clojure using the classpath specified in the `.clojure` file of the
# current directory.
#
# Mark Reid <http://mark.reid.name>
# CREATED: 2009-03-29
JAVA=
XDEBUG=-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=
USAGE="Usage: clj [-d debug-port] filename.clj"

# Attempt to find java automatically
if [ -z "$JAVA" ]; then
  # Attempt to find a suitable JAVA_HOME if we don't have one
  if [ -z "$JAVA_HOME" ]; then
    if [ -f /usr/libexec/java_home ]; then # OS X 10.5+
      JAVA_HOME=`/usr/libexec/java_home`
    fi
  fi
  
  if [ ! -z "$JAVA_HOME" ]; then # Found a JAVA_HOME, find java
    if $cygwin; then
      JAVA_HOME=`cygpath "$JAVA_HOME"`
    fi
    JAVA="$JAVA_HOME/bin/java"
  fi
fi

if [ -z "$JAVA" ] || [ ! -f "$JAVA" ]; then # Couldn't find java
  echo "Could not find java. Check \$JAVA_HOME or set \$JAVA on line 7."
  exit 1
fi

# Handle switches
while getopts "hd:" opt; do
  case $opt in
    d) DEBUGPORT=$OPTARG;;
    h) echo $USAGE
      exit 1;;
    \?) echo $USAGE
      exit 1;;
    *) echo $USAGE
      exit 1;;
  esac
done
shift $(($OPTIND - 1))

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

ARGS="-server"

# Add debug switch
if [[ "$DEBUGPORT" =~ ^[0-9]+$ ]]; then
  ARGS="$ARGS -Xdebug $XDEBUG$DEBUGPORT"
fi

# Cygwin-ify classpath
if $cygwin; then
  CP=`cygpath -wp "$CP"`
fi

if [ -z "$1" ]; then
  # Make jline and Cygwin cooperate with each other
  if $cygwin; then
    stty -icanon min 1 -echo
    REPL_FLAGS="-Djline.terminal=jline.UnixTerminal"
  fi

  "$JAVA" $ARGS -cp "$CP" $REPL_FLAGS jline.ConsoleRunner clojure.lang.Repl

  # Restore Cygwin TTY settings
    if $cygwin; then
    stty icanon echo
  fi
else
  scriptname=$1
  "$JAVA" $ARGS -cp "$CP" clojure.lang.Script $scriptname -- $*
fi
