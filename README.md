# clojure-bitly

Clojure mapping for the [Bitly API v3](http://code.google.com/p/bitly-api/wiki/ApiDocumentation#/v3).

## Usage

Declare the dependency for your project:

``` clojure
(defproject your-project "1.0.0-SNAPSHOT"
  :description "Your project description"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [clojure-bitly "0.1"]])
```
    
Then download dependencies:

    lein deps

Require:
 
``` clojure
(ns sample (:require [bitly :as bitly]))
```

Shorten a url:

``` clojure     
(def api-user "your-bitly-user")
(def api-key "your-bitly-apikey")
  
(bitly/with-auth api-user api-key
 (bitly/shorten "http://www.example.com/"))
```
      
    > "http://j.mp/j5YLIl"

``` clojure      
(bitly/with-auth api-user api-key
  (let [short-url (bitly/lookup "http://www.example.com/")]
    (bitly/clicks short-url)))
```
        
    > {:clicks [{:short_url "http://bit.ly/3hDSUb", :global_hash "3hDSUb", :user_clicks 956, :user_hash "3hDSUb", :global_clicks 956}]}


## Building

    lein deps
    lein jar

## License

Copyright (C) 2011 [Geoff Wilson](https://www.twitter.com/gmwils)

Distributed under the Eclipse Public License, the same as Clojure.
