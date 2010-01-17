ClojureX
========

Easy set up for Clojure.

**Supported Platforms**

  - Linux (tested on Ubuntu 9.10)
  - Mac OS X 10.5+
  - Windows (Cygwin)

Set Up Instructions
-------------------

	$ git clone git://github.com/citizen428/ClojureX.git clojure
	$ cd clojure

Grab all packages (clojure, clojure-contrib, jline and TextMate/Emacs support):

	$ git submodule init
	$ git submodule update

Build the packages:
	
	$ ant

To create a symlink for the `clj` script in `/usr/local/bin` you can run the following command:

    $ ./create_symlink

If you prefer to create the link somewhere else, you can do it manually like this:

	$ ln -s <full path to this project>/clj <destination path>/clj
	
To setup support for TextMate, run the following command which creates a symlink to the bundle in `~/Library/Application\ Support/TextMate/Bundles/`:

    $ ./configure_textmate
  
If you prefer Emacs for Clojure development the following command will add the necessary configuration for clojure-mode, slime and swank-clojure to your `~/.emacs`:

    $ ./configure_emacs

Usage
-----

The `clj` command can be used to open an interactive session:

	$ clj
	Clojure
	user=> 

It can be used to run a script:

	$ clj test.clj 
	Hello, Clojure!

or it can be used to make a script file executable by starting your file with this line:

	#!/usr/bin/env clj

then chmod u+x your file and run it.

You can also open a debug port like this:

    # clj -d 1234 test.clj
	
Note: The `clj` script expects `$JAVA_HOME` in its environment. If `$JAVA_HOME` is not set, it will attempt to determine it for you. Currently, automatic detection works on Mac OS X 10.5+ only. If for some reason you cannot provide `$JAVA_HOME`, you can set the `$JAVA` variable at the top of the `clj` script.

Working with Clojure
--------------------

To add extra jar files to Clojure's classpath on a project-by-project basis, just create a `.clojure` file in the project's directory. Here's an example: 

If your project directory is `~/code/clojure/cafe`, you can add the Grinder and Frother jars from the `~/code/clojure/cafe/lib` directory by putting their relative paths, separated by a colon, into the `.clojure` file:

	$ cd ~/code/clojure/cafe
	$ echo "lib/grinder.jar:lib/frother.jar" > .clojure

Staying up to date
------------------

Once you have a local checkout of ClojureX, it's easy to keep your Clojure installation up to date:

    $ cd clojure
    $ cd <submodule you want to update>
    $ git pull origin master
    
To update the source for all submodules at the same time, you can issue the following command:

    $ ./update_all

If there were any updates to clojure, clojure-contrib or jline you will have to rebuild them like this:

    $ ant

Todo
----

* Add scripts to work with Clojure projects after installation

Acknowledgements
----------------

* [Paul Rosania](http://github.com/paulrosania) for adding Cygwin support

* [Dave Barker](http://github.com/kzar) for adding debugging support (-d)

* [Carl Leiby](http://www.carlism.org/) for creating the [Clojure-MacOSX  project](http://github.com/carlism/Clojure-MacOSX/) which is the base for ClojureX

* [Mark Reid](http://mark.reid.name/) on which Carl's work was based

* [Tim Riddel](http://riddell.us/blog/) from whom I stole the contents of the .emacs file
