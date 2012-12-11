# dalap-html

dalap-html provides an easy yet powerful library to render your
templates using either Clojure (for server side rendering) or
Clojurescript (for client side rendering).

If you are familiar with hiccup/crate, then getting started with this
library should be really straightforward.

## Project Goals

_Another rendering library... really?_. Yep, although libraries out
there do a pretty good job, we were looking for more flexibility at
the time of managing our views. dalap-html provides:

* Different protocols that you can extend with your own types for
  maximum customization.

* Allows to transform/decorate your templates after definition by
  using something similar to CSS selectors (a la enlive).

* Provides automatic HTML escaping, if you want something to not be
  escaped, you have to be explicit about it.

## Project Maturity

dalap-html is a very young library; started in December 2011, used to
manage HTML templates and rendering for projects Birdseye Software
develops.

## Install

buster-cljs is released to clojars. If you are using maven, add the
following repository definition to your pom.xml

```xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

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

## Continuous Integration Status

[![Build Status](https://travis-ci.org/BirdseyeSoftware/dalap-html.png)](https://travis-ci.org/BirdseyeSoftware/dalap-html)

## License

Copyright © 2012 Birdseye Software

Distributed under the MIT License.

[getting_started]:https://birdseye-sw.com/oss/dalap-html/getting_started/
[documentation_site]:https://birdseye-sw.com/oss/dalap-html/
[test_suite]:https://github.com/BirdseyeSoftware/dalap-html/blob/master/test/clj/dalap/test/html_test.clj