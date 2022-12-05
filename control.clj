(ns control
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str]
            [hiccup2.core :refer [html]]
            [org.httpkit.server :as server]
            [cheshire.core :as json]
            [taoensso.timbre :as log]
            [org.httpkit.client :as client]

            [babashka.pods :as pods]
            [cognitect.transit :as transit])
  (:import
   [java.io ByteArrayInputStream ByteArrayOutputStream]))

(pods/load-pod 'org.babashka/go-sqlite3 "0.1.0")
(require '[pod.babashka.go-sqlite3 :as sqlite])

(defn transit-str [inp]
  (def out (ByteArrayOutputStream. 4096))
  (def writer (transit/writer out :json))
  
  (transit/write writer inp)
  (.toString out))

(defn transit-read [str]
  (def in (ByteArrayInputStream. (.getBytes str)))
  (def reader (transit/reader in :json))
  
  (transit/read reader))

(def config (clojure.edn/read-string (slurp (case (System/getProperty "user.name")
                                              "tommy" "config-dev.edn"
                                              "config.edn"))))

(def db (:mge-db config))
(def homeserver-url (:homeserver-url config))

(comment
  (sqlite/query db ["select (name) from sqlite_schema"])

  (sqlite/query db ["select * from mgemod_stats"])
  (sqlite/query db ["select * from players_in_server"]))

"lightweight web sever control node for this mge server.
  it listens for matches and commands from mge.tf, and implements them. (mostly through sqlite commands)


on startup, sends a request to mge.tf announcing itself. mge.tf pings every server and keeps track of the network. ?

comamnds mge.tf can send to this server.
======

get status (returns document with {:tournament/id }
get permissions
  sees server setting for who can start tournaments from mge.tf ui. (steamids)
  gets steamids through the sm_admin database, or through a config. can be :anybody, :nobody, :users [STEAMID_0:1123123], :admin (for my servers)

get players_active

post open_tournament(start_automatically?=true)
  changes map to tournament map
  sets server in a state where !add menu only has two arenas. 1) participate. and !remove

post start_tournament
  includes list of matchups that need to be played. (optionally includes map, classes, etc)

post player_warning
  when a match has taken too long to start, a 5 minute forfeit warning is sent.
   

commands this can send to mge.tf
======= "

(defn router [req]
  (let [paths (vec (rest (str/split (:uri req) #"/")))]
    (match [(:request-method req) paths]
           
           [:get ["api"]]
           {:body (json/generate-string (sqlite/query db ["select (name) from sqlite_schema"]))}
           
           
           [:get ["users" id]]
           {:body (str (html [:div id]))}
           
           :else {:body (str (html [:html "Welcome!"]))})))



(log/info "starting server with config" (pr-str config))

(defonce server (server/run-server router (or {:port 8091}
                                           (select-keys config [:port]))))

(defn test-server [method url body]
  (json/parse-string (:body @(client/request (cond-> {:method method
                                                      :url (str "http://0.0.0.0:3000/" url)
                                                      :as :text}
                             body (assoc :body body))))
                     keyword))

(defn query-homeserver [query]
  (let [resp @(client/request {:method :post
                               :url homeserver-url
                               :as :text
                               :headers {"content-type" "application/transit+json"}
                               :body (transit-str query) })]
    (def t resp)
    (println (:body t))
    (transit-read (:body resp))))

(comment
  (test-server :get "api/" nil)
  (query-homeserver '[(app.model.mge-servers/swag {:server/id 123})])

  (def registration (query-homeserver '[(app.model.mge-servers/register {:server/remote-addr "127.0.0.1"})]))
  
  (def id #uuid "a82082bc-d701-40e8-b8b9-bd00942a800b")
  
  (query-homeserver `[(app.model.mge-servers/ping {:server/id ~id})])
  (query-homeserver `[{(app.model.session/login
                        {:username "thmorriss@gmail.com", :password "arst"})
                       [:session/valid? :account/name]}])

  (query-homeserver `[{:servers/registered [:server/id :server/last-pinged]}])
  
  t)

@(promise)
