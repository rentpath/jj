# jj - Concise JSON Generator (Alpha)

[![Build Status](https://travis-ci.org/rentpath/jj.svg?branch=master)](https://travis-ci.org/rentpath/jj)

Produce JSON. Concisely. Particularly helpful for JSON that is deeply nested in multiple places.

## System Requirements

One or both:

- Java Runtime Environment (JRE)
- Node.js

Two usage patterns are supported:

- **CLI**: Uses Node.js. Pass in jj programs, get back JSON.
- **REPL**: Uses JVM. Input jj forms, get back JSON.

## Usage

- [Download a release](https://github.com/rentpath/jj/releases)
- Put `jj` on your PATH.
- Try examples below.

The release includes a JAR that provides a REPL if you run `java -jar jj-repl.jar` at the command-line.

### CLI Examples

```
jj \
'a:b:c:d:e' \
'a:b:c:y:z'
```

This will print to stdout:

```json
{
  "a": {
    "b": {
      "c": {
        "d": "e",
        "y": "z"
      }
    }
  }
}
```

If you're working with something like Elasticsearch, having shorter symbols for frequently-used parts of the JSON query structure can make it easier to view and internalize large queries. Here's an example using the built-in Elasticsearch mode:

```
jj -m es \
'q:b:s:[x:query y:query]' \
'q:b:m:[z:query]'
```

Which prints:

```json
{
  "query": {
    "bool": {
      "should": [
        {
          "x": "query"
        },
        {
          "y": "query"
        }
      ],
      "must": [
        {
          "z": "query"
        }
      ]
    }
  }
}
```

You can also define your own custom modes:

```
jj a:alpha,b:beta > custom-mode.json
jj -m custom-mode.json a:b:c
```

This will output:

```json
{
  "alpha": {
    "beta": "c"
  }
}
```

You can override built-in modes by putting your custom mode into a file of the same name and passing it to the `-m/--mode` option as shown above.

### REPL Examples

The REPL provides readline capabilities and maintains a history of commands.

Special commands include:

- `jj/quit` -- Quits the REPL process. You can also press `Ctrl-D`.
- `jj/reset` -- Resets the REPL state. If you're entering things but nothing is printing, try this.
- `jj/es-mode` or `jj/elasticsearch-mode` -- Defines short symbols for common Elasticsearch JSON keys. See [this namespace](https://github.com/rentpath/jj/blob/master/src/com/rentpath/jj/elasticsearch.cljc) for details.
- `def base a:b:c:d:e` -- Assigns the symbol `base` the JSON value produced by `a:b:c:d:e`. You can then use `base` in subsequent expressions, e.g., `base a:b:y:z` to add a `"y": "z"` entry to your object at key `b`.
- `ids:[1,2,3,4] jq ".ids"` -- Execute a jq query (via [jackson-jq](https://github.com/eiiches/jackson-jq)) of `.ids` against the JSON produced by `ids:[1,2,3,4]`, returning `[1,2,3,4]`.

**If the REPL gets into a weird state,** enter `jj/reset` and everything should be fine again. This will also remove anything you have assigned via `def`.

## Syntax

See the `com.rentpath.jj.parser` namespace for jj's grammar. Regular JSON is supported, but in addition:

- Double quotes are optional
- Commas are optional
- Curly braces are optional

## Contributing

System requirements:

- JDK
- Node.js
- Leiningen

To understand the code base:

- Review the scripts under `script/`.
- Boot up a REPL and take a look at the `com.rentpath.jj` namespace to start.

Main libraries used:

- [instaparse](https://github.com/engelberg/instaparse)
- [rebel-readline](https://github.com/bhauman/rebel-readline)
- [cheshire](https://github.com/dakrone/cheshire)

## Todos

- [ ] Rust implementation for CLI.
- [ ] Testing via ClojureScript
- [x] ~~Flesh out CLI args, starting with ability to specify reserved symbol sets (e.g., for Elasticsearch)~~
- [x] ~~Incorporate [jackson-jq](https://github.com/eiiches/jackson-jq) for richer REPL experience~~
- [ ] Fully support multi-line input at REPL
- [x] ~~Support custom mode file in JSON (show example using jj to create it) in CLI~~
- [x] ~~Support loading custom modes at the REPL~~
- [x] ~~Support stdin of JSON via CLI~~
- [ ] Support reading a file of JSON in via REPL

## License

Copyright 2018 RentPath, LLC

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
