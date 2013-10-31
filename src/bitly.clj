(ns bitly
  (:require [clj-http.client :as client]
            [clj-http.util :as util]
            [clojure.data.json :as json]))

(def ^:dynamic *api-user* nil)
(def ^:dynamic *api-key* nil)
(def ^:dynamic *api-base* "http://api.bitly.com/v3/")

(defn generate-query-string [params]
  "Generate query string allowing for duplicate parameters"
  (if (map? params)
    (client/generate-query-string params)
    (clojure.string/join "&"
             (loop [params params query nil]
               (if params
                 (let [k (first params)
                       v (second params)
                       new-query (conj query (str (util/url-encode (name k)) "="
                                                  (util/url-encode (str v))))]
                   (recur (nnext params) new-query))
                 query)))))

(defn- expand-args [k values]
  "Expand out the arguments for multiple values"
  (if (coll? values)
    (loop [vals values result nil]
      (if vals
        (recur (next vals) (concat [k (first vals)] result))
        result))
    [k values]))

(defn- build-request-url [method params]
  (if (and *api-user* *api-key*)
    (let [base-params ["login" *api-user*
                       "apiKey" *api-key*
                       "format" "json"]
         base-url *api-base*
         full-params (concat base-params params)]
     (str base-url method "?"
          (generate-query-string full-params)))
   (throw (IllegalArgumentException. "Must supply a Bitly API user and key.")))) 

(defn- request-data [url]
  (:data (json/read-str (:body (client/get url)))))

(defmacro with-auth
  "Sets the API user and API key for Bitly API requests"
  [api-user api-key & body]
  `(binding [*api-user* ~api-user
             *api-key* ~api-key]
     (do
       ~@body)))

;; API methods - http://code.google.com/p/bitly-api/wiki/ApiDocumentation#/v3
(defn shorten [url & {:keys [domain] :or {domain "j.mp"}}]
  (let [request-url (build-request-url "shorten" ["longUrl" url "domain" domain])]
    (:url (request-data request-url))))

(defn expand [short-url]
  (let [request-url (build-request-url "expand" ["shortUrl" short-url])]
    (request-data request-url)))

(defn validate [x_login x_apiKey]
  (let [request-url (build-request-url "validate" ["x_login" x_login
                                                   "x_apiKey" x_apiKey])]
    (request-data request-url)))

(defn clicks [short-urls]
  (if (and (coll? short-urls) (> (count short-urls) 15))
    (throw (IllegalArgumentException.
            "Must not supply more than 15 urls to Bitly clicks."))
    (let [request-url (build-request-url "clicks"
                                         (expand-args "shortUrl" short-urls))]
     (:clicks (request-data request-url)))))


(defn user-clicks []
  (let [request-url (build-request-url "user/clicks"
                                       (expand-args "unit" "month"))]
    (:clicks (request-data request-url))))

(defn clicks-by-minute [short-urls]
  (if (and (coll? short-urls) (> (count short-urls) 15))
    (throw (IllegalArgumentException.
            "Must not supply more than 15 urls to Bitly clicks-by-minute."))
   (let [request-url (build-request-url "clicks_by_minute"
                                        (expand-args "shortUrl" short-urls))]
     (:clicks_by_minute (request-data request-url)))))

(defn clicks-by-day [short-urls]
  (if (and (coll? short-urls) (> (count short-urls) 15))
    (throw (IllegalArgumentException.
            "Must not supply more than 15 urls to Bitly clicks-by-day."))
    (let [request-url (build-request-url "clicks_by_day"
                                         (expand-args "shortUrl" short-urls))]
      (request-data request-url))))

(defn bitly-pro-domain [domain]
  (let [request-url (build-request-url "bitly_pro_domain" ["domain" domain])]
    (request-data request-url)))

(defn lookup [long-urls]
  (if (and (coll? long-urls) (> (count long-urls) 15))
    (throw
     (IllegalArgumentException. "Must not supply more than 15 urls to Bitly lookup."))
    (let [request-url (build-request-url "lookup" (expand-args "url" long-urls))
          results (:lookup (request-data request-url))]
      (if (coll? long-urls)
        results
        (:short_url (first results))))))

(defn info [short-url]
  (let [request-url (build-request-url "info" ["shortUrl" short-url])]
    (request-data request-url)))
