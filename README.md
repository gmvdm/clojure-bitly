# clojure-bitly

Clojure mapping for the [Bitly API v3](http://code.google.com/p/bitly-api/wiki/ApiDocumentation#/v3).

## Usage
    
    (require 'bitly)
  
    (def api-user "your-bitly-user")
    (def api-key "your-bitly-apikey")
  
    (with-auth api-user api-key
      (shorten "http://www.example.com/"))
      
    > "http://j.mp/j5YLIl"
      
    (with-auth api-user api-key
      (let [short-url (lookup "http://www.example.com/")]
        (clicks short-url)))
        
    > {:clicks [{:short_url "http://bit.ly/3hDSUb", :global_hash "3hDSUb", :user_clicks 956, :user_hash "3hDSUb", :global_clicks 956}]}

## Building

    lein deps
    lein jar

## License

Copyright (C) 2011 [Geoff Wilson](https://www.twitter.com/gmwils)

Distributed under the Eclipse Public License, the same as Clojure.
