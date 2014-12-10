<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#sec-1">1. an autocompletion daemon for the Java programming language</a></li>
<li><a href="#sec-2">2. setup</a>
<ul>
<li><a href="#sec-2-1">2.1. start the daemon</a></li>
<li><a href="#sec-2-2">2.2. emacs setup</a></li>
</ul>
</li>
</ul>
</div>
</div>

# an autocompletion daemon for the Java programming language<a id="sec-1" name="sec-1"></a>

This completion daemon provides ide like functionality. The set of
features that are currently implemented:

-   code completion
-   method/field/variable signature lookup
-   adding import declarations

planed features are

-   removing unused imports

# setup<a id="sec-2" name="sec-2"></a>

The application currently holds dependencies on the following packages:

-   javaparser [code.google.com/p/javaparser/](https://code.google.com/p/javaparser/)
-   javassist [javassist.ortg](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/)
-   tinlylog [www.tinylog.org/](http://www.tinylog.org/)
-   junixsocket [code.google.com/p/junixsocket/](https://code.google.com/p/junixsocket/)
-   jsonp [jsonp.java.net/](https://jsonp.java.net/)

I am planning to replace javaparser with the eclipse astparser in
the long run (mostly to support java 8), but for now it serves me
well. To compile javacomplete with the provides gradle build file
all those dependencies needs to be saved to the libs/ dirctory in
the project root.

## start the daemon<a id="sec-2-1" name="sec-2-1"></a>

in order for the import feature to work with classes living in the
standard library you have to set the JAVASRC environment
variable pointing at the jdk source.

Dependencies may be referenced using the JAVACOMPLETPATH variable
(using a colon seperated list).

example call:

JAVASRC=/usr/lib/jvm/java-7-openjdk/src.zip \\
  JAVACOMPLETEPATH=build/libs/javacomplete-all-0.1.0.jar \\
  java -jar build/libs/javacomplete-all-0.1.0.jar

## emacs setup<a id="sec-2-2" name="sec-2-2"></a>

using [github.com/jostillmanns/javacomplete.el](https://github.com/jostillmanns/javacomplete.el)