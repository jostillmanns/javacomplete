<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#sec-1">1. communication documentation</a></li>
<li><a href="#sec-2">2. api call</a>
<ul>
<li><a href="#sec-2-1">2.1. expression</a></li>
<li><a href="#sec-2-2">2.2. prefix</a></li>
<li><a href="#sec-2-3">2.3. apicall</a></li>
<li><a href="#sec-2-4">2.4. line</a></li>
<li><a href="#sec-2-5">2.5. buffer</a></li>
</ul>
</li>
<li><a href="#sec-3">3. return value</a></li>
</ul>
</div>
</div>

# communication documentation<a id="sec-1" name="sec-1"></a>

The backend uses a unix socket ($TMP/javacomplete.sock) to
communicate with the client.

# api call<a id="sec-2" name="sec-2"></a>

Currently planned / implemented are the following api calls:

-   complete
-   cleanimports
-   addimport
-   definition

A call has to be made in json encoding with the format:

    {
        "file":"filename",
        "expression":"expression",
        "prefix":"prefix",
        "apicall":"complete",
        "line":0,
        "buffer":"buffercontent"
    }

## expression<a id="sec-2-1" name="sec-2-1"></a>

Contains the *current statement* without the *prefix* that will be
completed. The statement can be a parameter list, a block statement
*(for, while, if)* or anything that can be parsed using the
ExpressionParser class.

## prefix<a id="sec-2-2" name="sec-2-2"></a>

Contains the *prefix* that will be completed. Can be empty.

## apicall<a id="sec-2-3" name="sec-2-3"></a>

One of the apicalls enumerated above.

## line<a id="sec-2-4" name="sec-2-4"></a>

The current line inside the buffer.

## buffer<a id="sec-2-5" name="sec-2-5"></a>

The buffer. Currently the buffer needs to be correct syntax. I am
planning to replace the parser with the eclipse ASTParser, which
will allow to parse incomplete / incorrect syntax.

# return value<a id="sec-3" name="sec-3"></a>

The backend yields an integer followed by a newline ('\n') seperated
list of the following format:

NAME!TYPE!PARAMETER

The number of items are given by the integer in the first line. The
parameters are formated in a comma seperated list.