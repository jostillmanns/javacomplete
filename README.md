<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#sec-1">1. An autocompletion daemon for the Java programming language</a></li>
<li><a href="#sec-2">2. Builds</a></li>
<li><a href="#sec-3">3. Setup</a>
<ul>
<li><a href="#sec-3-1">3.1. Junixsocket native lib</a></li>
<li><a href="#sec-3-2">3.2. Start the daemon</a></li>
<li><a href="#sec-3-3">3.3. Emacs setup</a></li>
</ul>
</li>
</ul>
</div>
</div>

# An autocompletion daemon for the Java programming language<a id="sec-1" name="sec-1"></a>

The completion daemon provides ide like functionality. Development
is at a very early stage. The completion definitely can be improved and
is likely not to work in some corner cases. Nevertheless I am
already using the completion for development, so I think it's in a
useable state.

The following features are currently implemented:

-   code completion
-   method/field/variable signature lookup
-   adding import declarations

planed features are

-   removing unused imports
-   &#x2026;

# Builds<a id="sec-2" name="sec-2"></a>

available on [tillmanns.me](http://www.tillmanns.me/)

# Setup<a id="sec-3" name="sec-3"></a>

The application currently holds dependencies on the following packages:

-   javaparser [code.google.com/p/javaparser/](https://code.google.com/p/javaparser/)
-   javassist [javassist.ortg](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/)
-   tinlylog [www.tinylog.org/](http://www.tinylog.org/)
-   junixsocket [code.google.com/p/junixsocket/](https://code.google.com/p/junixsocket/)
-   jsonp [jsonp.java.net/](https://jsonp.java.net/)

I am planning to replace javaparser with the eclipse astparser on
the long run (mostly to support java 8) but for now it serves me
well. To compile javacomplete with the provided gradle build file
all these dependencies need to be saved to the libs/ dirctory in the
project root.

## Junixsocket native lib<a id="sec-3-1" name="sec-3-1"></a>

You can download the library files here:
[junixsocket/downloads/list](https://code.google.com/p/junixsocket/downloads/list)

Instructions on how to install can be found here:
[junixsocket/wiki/GettingStarted](https://code.google.com/p/junixsocket/wiki/GettingStarted)

You have to copy the .so files to the LIBRARY<sub>PATH</sub>, which is
/opt/newsclub/lib-native by default. The easiest way to setup them
is to use the default path.

## Start the daemon<a id="sec-3-2" name="sec-3-2"></a>

In order for the import feature to work with classes living in the
standard library you have to set the JAVASRC environment variable
pointing at the jdk source.

Dependencies may be referenced using the JAVACOMPLETPATH variable
(using a colon seperated list).

example call:

    JAVASRC=/usr/lib/jvm/java-7-openjdk/src.zip \
         JAVACOMPLETEPATH=build/libs/javacomplete-all-0.1.0.jar \
         java -jar build/libs/javacomplete-all-0.1.0.jar

## Emacs setup<a id="sec-3-3" name="sec-3-3"></a>

using [github.com/jostillmanns/javacomplete.el](https://github.com/jostillmanns/javacomplete.el)