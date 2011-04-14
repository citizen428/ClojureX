ClojureX
==========

Easy set up for Clojure on unixoid operating systems.

**Tested Platforms**

- Mac OS X 10.5+
- OpenSolaris 
- Linux 
- Windows (Cygwin)

**Note**

I only test on MacOS 10.6 and Linux (Debian Squeeze) myself, since I
currently don't have an OpenSolaris machine. 

Set Up Instructions
-------------------

    $ git clone git://github.com/citizen428/ClojureX.git 
    $ cd ClojureX

To create a symlink for the `clj` script in `/usr/local/bin` you can run the following command:

    $ bin/create_symlink

If you prefer to create the link somewhere else, you can do it manually like this:

    $ ln -s <full path to this project>/clj <destination path>/clj

Note: The `clj` script expects `$JAVA_HOME` in its environment. If `$JAVA_HOME` is not set, it will attempt to determine it for you. If for some reason you cannot provide `$JAVA_HOME`, you can set the `$JAVA` variable at the top of the `clj` script.

Usage
-----

The `clj` command can be used to open an interactive session:

    $ clj
    Clojure 1.1.0-master-SNAPSHOT
    user=>

It can be used to run a script:

    $ clj test.clj
    Hello, Clojure!

Any options following the script will be passed as arguments to the script:

    $ clj test.clj a b "c d"
    Hello, Clojure!
    Arg #1: a
    Arg #2: b
    Arg #3: c d

Use a [shebang line][shebang] at the start of your script to make a Clojure file executable:

    #!/usr/bin/env clj

Of course, you'll also need to enable the script's execute mode (e.g., `chmod u+x <scriptname>`) to run it this way.

You can also open a debug port using the `-d` or `--debug` options:

    $ clj -d 1234 test.clj

Additionally, the `clj` script supports all of `clojure.main`'s command-line options. For example, to evaluate an expression, use `-e` or `--eval`. The script's value will be sent to standard output:

    $ clj -e "(take 5 (iterate inc 0))"
    (0 1 2 3 4)
    $ clj --eval '(count "Hello, Clojure")'
    14

Use the `-i` or `--init` option to evaluate a clojure script:

    $ clj -i test.clj
    Hello, Clojure

You can intermix the eval and init options multiple times, and they will be evaluated in the specified order:

    $ clj -e '"Before test"' -i test.clj -e '"After test"'
    "Before test"
    Hello, Clojure!
    "After test"

The `clj` script will exit immediate after processing all the init/eval options. Use `-r` or `--repl` to instead start an interactive session.

    $ clj -e '"Starting my own REPL"' -r
    "Starting my own REPL"
    user=>

Finally, `clj` supports all of the java command line options to configure the JVM.

    $ clj -d64 -Xms4g -Xmx4g -verbose:gc -i wf2.clj -e '(wf/wf-atoms "O.all")'

To see a full description of `clj` command-line options, pass it `-?`, `-h`, or `--help`

    $ clj --help
    Usage: clj ...

Working with Clojure
--------------------

To add extra jar files to Clojure's classpath on a project-by-project basis, just create a `.clojure` file in the project's directory. Here's an example:

If your project directory is `~/code/clojure/cafe`, you can add the Grinder and Frother jars from the `~/code/clojure/cafe/lib` directory by putting their relative paths, separated by a colon, into the `.clojure` file:

    $ cd ~/code/clojure/cafe
    $ echo "lib/grinder.jar:lib/frother.jar" > .clojure

You can also list jars one per line in the .clojure file, like so:

    $ cd ~/code/clojure/cafe
    $ ls -1 lib/*.jar > .clojure

Todo
----

* <del>Add scripts to work with Clojure projects after installation</del> - Taken care of by [Leiningen](http://github.com/technomancy/leiningen/blob/master/README.md)

Acknowledgements
----------------
* [Scott Haug](http://github.com/shaug) for lots of new features for the clj script

* [Paul Rosania](http://github.com/paulrosania) for adding Cygwin support

* [Dave Barker](http://github.com/kzar) for adding debugging support (-d)

* [Carl Leiby](http://www.carlism.org/) for creating the [Clojure-MacOSX  project](http://github.com/carlism/Clojure-MacOSX/) which is the base for ClojureX

* [Mark Reid](http://mark.reid.name/) on which Carl's work was based

* [Tim Riddel](http://riddell.us/blog/) from whom I stole the contents of the .emacs file

[shebang]:  http://en.wikipedia.org/wiki/Shebang_(Unix)
