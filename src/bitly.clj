(ns bitly
  (:use [clojure.contrib.json :only (json-str read-json)])
  (:require [clj-http.client :as client]))

(def *api-user* nil)
(def *api-key* nil)
(def *api-base* "http://api.bitly.com/v3/")

(defn- build-request-url [method params]
  (let [base-params {"login" *api-user*
                     "apiKey" *api-key*
                     "format" "json"}
        base-url *api-base*
        full-params (merge base-params params)]
    (str base-url method "?"
         (client/generate-query-string full-params))))

(defn- request-data [url]
  (:data (read-json (:body (client/get url)))))

(defmacro with-auth
  "Sets the API user and API key for Bitly API requests"
  [api-user api-key & body]
  `(binding [*api-user* ~api-user
             *api-key* ~api-key]
     (do
       ~@body)))

;; API methods - http://code.google.com/p/bitly-api/wiki/ApiDocumentation#/v3
(defn shorten [url]
  (let [request-url (build-request-url "shorten" {"longUrl" url "domain" "j.mp"})]
    (:url (request-data request-url))))

(defn expand [short-url]
  (let [request-url (build-request-url "expand" {"shortUrl" short-url})]
    (request-data request-url)))

(defn validate [x_login x_apiKey]
  (let [request-url (build-request-url "validate" {"x_login" x_login
                                                   "x_apiKey" x_apiKey})]
    (request-data request-url)))

                                        ; TODO - support multiple urls or hashes (up to 15)
(defn clicks [short-url]
  (let [request-url (build-request-url "clicks" {"shortUrl" short-url})]
    (request-data request-url)))

                                        ; TODO - support multiple urls or hashes (up to 15)
(defn clicks-by-minute [short-url]
  (let [request-url (build-request-url "clicks_by_minute" {"shortUrl" short-url})]
    (request-data request-url)))

                                        ; TODO - support multiple urls or hashes (up to 15)
(defn clicks-by-day [short-url]
  (let [request-url (build-request-url "clicks_by_day" {"shortUrl" short-url})]
    (request-data request-url)))

(defn bitly-pro-domain [domain]
  (let [request-url (build-request-url "bitly_pro_domain" {"domain" domain})]
    (request-data request-url)))

                                        ; TODO - support multiple urls
(defn lookup [long-url]
  (let [request-url (build-request-url "lookup" {"url" long-url})]
    (:short_url
     (first (:lookup (request-data request-url))))))

(defn info [short-url]
  (let [request-url (build-request-url "info" {"shortUrl" short-url})]
    (request-data request-url)))
