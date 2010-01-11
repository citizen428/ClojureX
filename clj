#!/bin/bash
# Runs Clojure using the classpath specified in the `.clojure` file of the
# current directory.
#
# Original version by Mark Reid <http://mark.reid.name>
# CREATED: 2009-03-29
JAVA=
XDEBUG=-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=
PRG_NAME=`basename $0`
USAGE="Usage: $PRG_NAME [JAVAOPTS] [-e code] [-i filename.clj] [-d debug-port] [filename.clj args...]"

avail() {
  type -P $1 $>/dev/null
}

usage() {
  echo $USAGE
}

arg_check() {
  if [[ $2 -lt 2 ]]; then
    echo "$PRG_NAME: option requires an argument -- $1" >&2
    usage
    exit 1
  fi
}

# Detect environments (just Cygwin for now)
cygwin=false
case "`uname`" in
  CYGWIN*) cygwin=true;;
esac

# Attempt to find java automatically
if [ -z "$JAVA" ]; then
  # Attempt to find a suitable JAVA_HOME if we don't have one
  if [ -z "$JAVA_HOME" ]; then
    if [ -f /usr/libexec/java_home ]; then # OS X 10.5+
      JAVA_HOME=`/usr/libexec/java_home`
    fi
  fi
  
  if [ -n "$JAVA_HOME" ]; then # Found a JAVA_HOME, find java
    if $cygwin; then
      JAVA_HOME=`cygpath "$JAVA_HOME"`
    fi
    JAVA="$JAVA_HOME/bin/java"
  else
    # last ditch -- look for java on the path
    JAVA=`type -P java`
  fi
fi

if [ -z "$JAVA" ] || [ ! -f "$JAVA" ]; then # Couldn't find java
  echo "Could not find Java. Check \$JAVA_HOME or set \$JAVA in this script."
  exit 1
fi

JAVA_ARGS="-server"
CLJ_ARGS=""
FILE_ARGS=""

while [ $# -gt 0 ] ; do
  if [ -n "$FILE_ARGS" ]; then
    # if we've started capturing FILE_ARGS, then all remaining options are part of FILE_ARGS
    FILE_ARGS="$FILE_ARGS $(printf "%q" "$1")"
  else
    case "$1" in
    -h|--help|-\?)
      usage
      exit 1
      ;;
    -cp|-classpath)
      # make sure there's a second argument
      arg_check $1 $#
      # capture classpath separately from other java args since we're already building up a classpath
      CP="$CP:$2"
      # a separate shift for the second argument
      shift
      ;;
    -d)
      # make sure there's a second argument
      arg_check $1 $#
      # Add debug switch
      if [[ "$2" =~ ^[0-9]+$ ]]; then
        JAVA_ARGS="$JAVA_ARGS -Xdebug $XDEBUG$2"
      else
        echo "$PRG_NAME: debug port must be an integer -- $2"
        usage
        exit 1
      fi
      shift
      ;;
    -e|--eval|-i|--init) # CLJ arguments
      # make sure there's a second argument
      arg_check $1 $#
      # use printf to preserve existing quotes on the command line correctly
      CLJ_ARGS="$CLJ_ARGS $1 $(printf "%q" "$2")"
      shift
      ;;
    -server)
      # ignore. -server is set by default.
      ;;
    -*)
      # assume any other switches are java switches
      JAVA_ARGS="$JAVA_ARGS $1"
      ;;
    *)
      # not a switch. must be a file for clojure to process.
      FILE_ARGS="$(printf "%q" "$1")"
      ;;
    esac
  fi
  shift
done


# resolve links - $0 may be a softlink
PRG="$0"
while [ -h "$PRG" ]; do
  # if readlink is availble, use it; it is less fragile than relying on `ls` output
  if avail readlink; then
    PRG=`readlink "$PRG"`
  else
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG=`dirname "$PRG"`/"$link"
    fi
  fi
done

CLJ_DIR=`dirname "$PRG"`
CLOJURE=$CLJ_DIR/clojure/clojure.jar
CONTRIB=$CLJ_DIR/clojure-contrib/clojure-contrib.jar
JLINE=$CLJ_DIR/jline/jline.jar
CP=$PWD:$CLOJURE:$CONTRIB

# Add extra jars as specified by `.clojure` file
if [ -f .clojure ]
then
  if avail tr; then
    # support jars on multiple lines if 'tr' is available
    CP="$CP:`tr '\n' ':' < .clojure`"
  else
    CP="$CP:`cat .clojure`"
  fi
fi

# determine if we should fire up the REPL, and if so, which kind
REPL=""
# if there are closure arguments or a file to process, then no need for a REPL
if [ -z "$CLJ_ARGS" -a -z "$FILE_ARGS" ]; then
  if avail rlwrap; then
    REPL="rlwrap"
  else
    REPL="jline"
    CP="$CP:$JLINE"
  fi
fi

# Cygwin-ify classpath
if $cygwin; then
  CP=`cygpath -wp "$CP"`
fi

case $REPL in
rlwrap )
  # used by rlwrap to determine which characters determine a 'word'
  BREAK_CHARS="\(\){}[],^%$#@\"\";:''|\\"

  # determine the dictionary of completions to use with rlwrap. prefer the users own
  CLJ_COMP="$HOME/.clojure-completions"
  if [ ! -e "$CLJ_COMP" ]; then
    CLJ_COMP="$CLJ_DIR/clojure-completions"
  fi

  eval rlwrap --remember -c -b "$(printf "%q" "$BREAK_CHARS")" -f "$CLJ_COMP" java $JAVA_ARGS -cp "$CP" clojure.main
  ;;
jline )
  # Make jline and Cygwin cooperate with each other
  if $cygwin; then
    trap "stty `stty -g` >/dev/null" EXIT # Restore TTY settings on exit
    stty -icanon min 1 -echo
    JAVA_ARGS="$JAVA_ARGS -Djline.terminal=jline.UnixTerminal"
  fi

  eval java $JAVA_ARGS -cp "$CP" jline.ConsoleRunner clojure.main
  ;;
*)
  eval java $JAVA_ARGS -cp "$CP" clojure.main $CLJ_ARGS $FILE_ARGS
  ;;
esac
