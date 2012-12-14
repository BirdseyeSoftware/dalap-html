# dalap-html [![Build Status](https://travis-ci.org/BirdseyeSoftware/dalap-html.png)](https://travis-ci.org/BirdseyeSoftware/dalap-html)

dalap-html is an html templating library that can be used from either
Clojure (for server side rendering) or Clojurescript (for client side
rendering).

If you have used hiccup/crate, the basic syntax and usage should be
familiar.

## Project Goals

_Another html rendering library... really?_. Although other libraries
do a pretty good job, we were looking for more flexibility
and a better cross-platform workflow. dalap-html:

* Provides CSS-like selectors which allow you to transform, decorate,
  and specialize your templates after definition. Write one set of
  templates and then use selectors to specialize them for different
  contexts. This is inspired by Enlive and CSS.

* Automatically escapes all HTML in strings. If you want something to
  not be escaped, you have to be explicit about it.

* Is very easy to extend and customize.

## Project Maturity

dalap-html is young, beta-stability library and is subject to change.

## Install

dalap-html is released to clojars.

### The most recent release

With leiningen:

```
[com.birdseye-sw/dalap-html "0.1.0"]
```

With Maven:

```xml
<dependency>
  <groupId>com.birdseye-sw</groupId>
  <artifactId>dalap-html</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Getting Started

Please refer to our [Getting Started guide][getting_started].

## Documentation & Examples

Please refer to our [documentation site][documentation_site], our
[test suite][test_suite] also has some examples

## Supported Clojure Versions

This library has been tested on Clojure 1.4 and Clojurescript version
that comes bundled with `lein-cljsbuild` 0.2.9

## License

Copyright © 2012 Birdseye Software Ltd.

Distributed under the MIT License.

[getting_started]:http://birdseyesoftware.github.com/dalap-html.docs/articles/getting_started.html
[documentation_site]:http://birdseye-sw.com/oss/dalap-html/
[test_suite]:https://github.com/BirdseyeSoftware/dalap-html/blob/master/test/clj/dalap/test/html_test.clj
