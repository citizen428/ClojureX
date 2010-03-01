#!/bin/bash
# Runs Clojure using the classpath specified in the `.clojure` file of the
# current directory.
#
# Original version by Mark Reid <http://mark.reid.name>
# CREATED: 2009-03-29
JAVA=
XDEBUG=-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=
PRG_NAME=`basename $0`
USAGE="Usage: $PRG_NAME [java-opt*] [init-opt*] [main-opt] [arg*]"

# determine if $1 is an available program (eschew 'which', as it's unreliable)
avail() {
  type -P $1 $>/dev/null
}

# send the stock usage text to stderr
usage() {
  echo $USAGE >&2
}

# show the provided error message, the usage information, a friendly tip for more info, and exit
error() {
  if [[ -n "$1" ]]; then
    echo $1 >&2
  fi
  usage
  echo >&2
  echo "Try '$PRG_NAME --help' for more options." >&2
  exit 1
}

help() {
  # spit out the standard usage text
  usage
  # generate clojure's own help text, stripping off the usage and adding a 'java options' section prior to init options
  java -cp "$CP" clojure.main --help 2>&1 | grep -v "^Usage" | sed 's/init options:/java options:\
    -<javaopt>        Configure JVM (see `java -help` for full list)\
    -d, --debug port  Open a port for debugging\
\
  init options:/' >&2
  exit 1
}

# $1 is the argument name, and $2 is the number of arguments left.
arg_check() {
  if [[ $2 -lt 2 ]]; then
    error "$PRG_NAME: option requires an argument -- $1"
  fi
}

# set REPL (if it isn't already set) to the best available repl type: rlwrap if it's in the path, otherwise jline
default_repl() {
  if [[ "" == "$REPL" ]]; then
    if avail rlwrap; then
      REPL="rlwrap"
    else
      REPL="jline"
    fi
  fi
}

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
CLOJURE=$CLJ_DIR/lib/clojure.jar
CONTRIB=$CLJ_DIR/lib/clojure-contrib.jar
JLINE=$CLJ_DIR/lib/jline.jar
CP=$PWD:$CLOJURE:$CONTRIB

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
  error "Could not find Java. Check \$JAVA_HOME or set \$JAVA in this script."
fi

JAVA_VM=""
JAVA_OPTS=""
INIT_OPTS=""
MAIN_OPTS=""
REPL="" # set this if we know for sure we want a REPL

while [ $# -gt 0 ] ; do
  if [ -n "$MAIN_OPTS" ]; then
    # if we've started capturing MAIN_OPTS, then all remaining arguments are part of MAIN_OPTS
    MAIN_OPTS="$MAIN_OPTS $(printf "%q" "$1")"
  else
    case "$1" in
    -h|--help|-\?)
      help
      ;;
    -cp|-classpath)
      # make sure there's a second argument
      arg_check $1 $#
      # capture classpath separately from other java args since we're already building up a classpath
      CP="$CP:$2"
      # a separate shift for the second argument
      shift
      ;;
    -d|--debug)
      # make sure there's a second argument
      arg_check $1 $#
      # Add debug switch
      if [[ "$2" =~ ^[0-9]+$ ]]; then
        JAVA_OPTS="$JAVA_OPTS -Xdebug $XDEBUG$2"
      else
        error "$PRG_NAME: debug port must be an integer -- $2"
      fi
      shift
      ;;
    -r|--repl)
      MAIN_OPTS="$MAIN_OPTS $1"
      default_repl
      ;;
    -R)
      # explcitly choose a REPL -- used for testing
      arg_check $1 $#
      case "$2" in
        clj|jline|rlwrap )  ;;
        *)                  error "$PRG_NAME: Unknown REPL -- $REPL" ;;
      esac
      MAIN_OPTS="$MAIN_OPTS -r"
      REPL="$2"
      shift
      ;;
    -)
      MAIN_OPTS="$MAIN_OPTS $1"
      ;;
    -e|--eval|-i|--init) # CLJ arguments
      # make sure there's a second argument
      arg_check $1 $#
      # use printf to preserve existing quotes on the command line correctly
      INIT_OPTS="$INIT_OPTS $1 $(printf "%q" "$2")"
      shift
      ;;
    -server)
      if [[ "$JAVA_VM" == "-client" ]]; then
        error "Cannot specify both -server and -client VMs"
      fi
      # redundant, but doesn't hurt any
      JAVA_VM="-server"
      ;;
    -client|-jvm|-hotspot)
      if [[ "$JAVA_VM" == "-server" ]]; then
        error "Cannot specify both -server and -client VMs"
      fi
      JAVA_VM="-client"
      ;;
    -*)
      # assume any other options are java options
      JAVA_OPTS="$JAVA_OPTS $1"
      ;;
    *)
      # not a switch. must be the path for clojure to process.
      MAIN_OPTS="$(printf "%q" "$1")"
      ;;
    esac
  fi
  shift
done

# specify the java VM to use, defaulting to -server if not specified
JAVA_OPTS="$JAVA_OPTS ${JAVA_VM:--server}"

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
# if there are init or main options, then no need to choose a REPL
if [ -z "$INIT_OPTS" -a -z "$MAIN_OPTS" ]; then
  default_repl
fi

# add jline jars if we're using jline for our REPL
if [[ "$REPL" == "jline" ]]; then
  CP="$CP:$JLINE"
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

  eval rlwrap --remember -c -b "$(printf "%q" "$BREAK_CHARS")" -f "$CLJ_COMP" java $JAVA_OPTS -cp "$CP" clojure.main $INIT_OPTS $MAIN_OPTS
  ;;
jline )
  # Make jline and Cygwin cooperate with each other
  if $cygwin; then
    trap "stty `stty -g` >/dev/null" EXIT # Restore TTY settings on exit
    stty -icanon min 1 -echo
    JAVA_OPTS="$JAVA_OPTS -Djline.terminal=jline.UnixTerminal"
  fi

  eval java $JAVA_OPTS -cp '$CP' jline.ConsoleRunner clojure.main $INIT_OPTS $MAIN_OPTS
  ;;
*)
  eval java $JAVA_OPTS -cp '$CP' clojure.main $INIT_OPTS $MAIN_OPTS
  ;;
esac
