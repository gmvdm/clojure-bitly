(ns bitly
  (:require [clj-http.client :as client]
            [clj-http.util :as util]
            [clojure.data.json :as json]))

(def ^:dynamic *access-token* nil)
(def ^:dynamic *api-base* "https://api-ssl.bitly.com/v3/")

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
  (if *access-token*
    (let [base-params ["access_token" *access-token*]
         base-url *api-base*
         full-params (concat base-params params)]
     (str base-url method "?"
          (generate-query-string full-params)))
   (throw (IllegalArgumentException. "Must supply a Bitly access token.")))) 

(defn- request-data [url]
  (get (json/read-str (:body (client/get url))) "data"))

(defmacro with-auth
  "Sets the API user and API key for Bitly API requests"
  [access-token & body]
  `(binding [*access-token* ~access-token]
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


(defn user-clicks 
  "Returns the aggregate number of clicks on all of the authenticated user's bitly links.

   Parameters:

   * unit - minute, hour, day, week or month, default: day 
      Note: when unit is minute the maximum value for units is 60.

   * units - an integer representing the time units to query data for. Pass -1 to return all units of time.
   * timezone - an integer hour offset from UTC (-14 to 14), or a timezone string default: America/New_York.
   * rollup - true or false. Return data for multiple units rolled up to a single result instead of a separate value for each period of time.
   * limit - 1 to 1000 (default=100).
   * unit_reference_ts - an epoch timestamp, indicating the most recent time for which to pull metrics, default: now. 
     Note: the value of unit_reference_ts rounds to the nearest unit. 
     Note: historical data is stored hourly beyond the most recent 60 minutes. If a unit_reference_ts is specified, unit cannot be minute.
"
  [& {:keys [unit units timezone rollup limit unit_reference_ts] :or {unit "day" units "7" timezone "0" rollup "false" limit "100" unit_reference_ts "now"}}]
  (let [request-url (build-request-url "user/clicks" ["unit" unit "units" units "timezone" timezone "rollup" rollup "limit" limit "unit_reference_ts" unit_reference_ts])]
    (request-data request-url)))


(defn user-popular-links
"Returns the authenticated user's most-clicked bitly links (ordered by number of clicks) in a given time period.
Parameters
unit - minute | hour | day | week | month default:day 
Note: when unit is minute the maximum value for units is 60
units = an integer representing the time units to query data for. pass -1 to return all units of time.
timezone - an integer hour offset from UTC (-14..14), or a timezone string default:America/New_York.
limit=1..1000 (default=100)
unit_reference_ts - an epoch timestamp, indicating the most recent time for which to pull metrics. default:now 
Note: the value of unit_reference_ts rounds to the nearest unit. 
Note: historical data is stored hourly beyond the most recent 60 minutes. If a unit_reference_ts is specified, unit cannot be minute.
"
[& {:keys [unit units timezone limit unit_reference_ts] :or {unit "day" units "7" timezone "0" limit "100" unit_reference_ts "now"}}]
(let [request-url (build-request-url "user/popular_links" ["unit" unit "units" units "timezone" timezone "limit" limit "unit_reference_ts" unit_reference_ts])]
    (request-data request-url)))

(defn user-countries
  "Returns aggregate metrics about the countries referring click traffic to all of the authenticated user's bitly links." []
  (let [request-url (build-request-url "user/countries" nil)]
    (request-data request-url)))


(defn user-info []
  (let [request-url (build-request-url "user/info" nil)]
    (request-data request-url)))

(defn user-link-history []
  (let [request-url (build-request-url "user/link_history" nil)]
    (request-data request-url)))

(defn user-network-history []
  (let [request-url (build-request-url "user/network_history" nil)]
    (request-data request-url)))

(defn user-tracking-domain-list []
  (let [request-url (build-request-url "user/tracking_domain_list" nil)]
    (request-data request-url)))



;; deprecated
(defn clicks-by-minute [short-urls]
  (if (and (coll? short-urls) (> (count short-urls) 15))
    (throw (IllegalArgumentException.
            "Must not supply more than 15 urls to Bitly clicks-by-minute."))
   (let [request-url (build-request-url "clicks_by_minute"
                                        (expand-args "shortUrl" short-urls))]
     (:clicks_by_minute (request-data request-url)))))

;; deprecated
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

;; deprecated
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

(defn expand [short-url]
  (let [request-url (build-request-url "expand" ["shortUrl" short-url])]
    (request-data request-url)))
