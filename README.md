# jj - Concise JSON Generator (Alpha)

Produce JSON. Concisely.

## Usage

System requirements (one or both):

- Java Runtime Environment (JRE)
- Node.js

Two usage patterns are supported:

- **CLI**: On JVM and Node.js. Pass in jj programs, get back JSON.
- **REPL**: On JVM. Input jj forms, get back JSON.

_We'd recommend using the Node.js CLI if possible, since it's smaller and faster to execute._

[Download a release](https://github.com/rentpath/jj/releases) and try running `./jj query:bool:must:[]` (requires Node.js) or `java -jar jj-repl.jar` (requires JRE).

## Examples

```
jj \
'query:bool:should:[a:query b:query]' \
'query:bool:must:  [a:query b:query]'
```

This will print to stdout:

```json
{
  "query": {
    "bool": {
      "should": [
        {
          "a": "query"
        },
        {
          "b": "query"
        }
      ],
      "must": [
        {
          "a": "query"
        },
        {
          "b": "query"
        }
      ]
    }
  }
}
```

## Syntax

See the `com.rentpath.jj.parser` namespace for jj's grammar. In general:

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

- [ ] Testing via ClojureScript
- [ ] Flesh out CLI args, starting with ability to specify reserved symbol sets (e.g., for Elasticsearch)
- [ ] Incorporate [jackson-jq](https://github.com/eiiches/jackson-jq) for richer REPL experience

## License

Copyright 2018 RentPath, LLC

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
