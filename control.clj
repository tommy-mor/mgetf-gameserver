(ns control
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str]
            [hiccup2.core :refer [html]]
            [org.httpkit.server :as server]
            [cheshire.core :as json]
            [taoensso.timbre :as log]
            [org.httpkit.client :as client]

            [babashka.pods :as pods]
            [cognitect.transit :as transit]
            [babashka.fs :as fs]
            [clojure.walk])
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

(def config (clojure.edn/read-string (slurp (case (System/getenv "PROD")
                                              nil "config-dev.edn"
                                              "config.edn"))))

(defn map->nsmap
  [m n]
  (clojure.walk/postwalk (fn [x] (if (keyword? x)
                                   (keyword n (name x))
                                   x))
                         m))

(def db (:mge-db config))
(def homeserver-url (:homeserver-url config))

(defn set-tournament [k v]
  (sqlite/execute! db
                   ["insert or replace into tournament (key, value) values (?, ?)" k v]))
(defn get-tournament [k]
  (:value (first (sqlite/query db
                               ["select (value) from tournament where key = ?" k]))))

(defn setup-db! []
  (sqlite/execute! db
                   ["CREATE TABLE IF NOT EXISTS matches (player1 TEXT, player2 TEXT)"])
  
  (sqlite/execute! db
                   ["delete from matches where true"])
  
  (sqlite/execute! db
                   ["CREATE TABLE IF NOT EXISTS players_in_server (name TEXT, steamid TEXT)"])
  (sqlite/execute! db
                   ["CREATE TABLE IF NOT EXISTS tournament (key TEXT PRIMARY KEY, value TEXT)"])
  (sqlite/query db ["select (value) from tournament where key = 'status'"])
  (sqlite/execute! db
                   ["CREATE TABLE IF NOT EXISTS mgemod_duels (winner TEXT, loser TEXT, winnerscore INTEGER, loserscore INTEGER, winlimit INTEGER, gametime INTEGER, mapname TEXT, arenaname TEXT)"])
  
  #_(sqlite/execute! db ["delete from mgemod_duels where true"]))


(when (and (:dev config) (fs/exists? db))
  (fs/delete db)
  (setup-db!)
  (doall
   (for [[name steamid] [["hallu" "halluid"]
                         ["tommy" "tommyid"]
                         ["kier" "kierid"]]]
     (sqlite/execute! db
                      ["insert into players_in_server (name, steamid) values (?, ?)"
                       name steamid]))))

(comment
  (sqlite/query db ["select (name) from sqlite_schema"])

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

(defn players-in-server []
  (map->nsmap
   (sqlite/query db ["select * from players_in_server"])
   "player"))

(defn router [req]
  
  (let [paths (vec (rest (str/split (:uri req) #"/")))
        body (json/parse-string (when (:body req) (slurp (:body req))) keyword)]
    (match [(:request-method req) paths]
           [:get ["api"]]
           {:body (do
                    "TODO make this query the gameserver"
                    (json/generate-string {:status "ok"})) }

           [:get ["api" "current-tournament"]]
           {:body
            (json/generate-string {:tournament/id (get-tournament ":tournament/id")})}
           
           [:post ["api" "start-tournament"]]
           {:body
            (let [tid (:tournament/id body)]
              (set-tournament ":tournament/id" tid)
              (json/generate-string {:tournament/id tid
                                     :tournament/participants (players-in-server)}))}
           
           [:get ["api" "players"]]
           {:body (json/generate-string (players-in-server))}
           
           
           :else {:body (str (html [:html "Welcome!"]))})))



(log/info "starting server with config" (pr-str config))

(def server (server/run-server router (select-keys config [:port])))

(defn test-server [method url body]
  (let [req @(client/request (cond-> {:method method
                                      :url (str "http://0.0.0.0:"
                                                (:port config) "/"
                                                url)
                                      :as :text}
                               body (assoc :body body)))]
    (def t req)
    (json/parse-string (:body req)
                       keyword)))

(defn query-homeserver [query]
  (let [resp @(client/request {:method :post
                               :url homeserver-url
                               :as :text
                               :headers {"content-type" "application/transit+json"}
                               :body (transit-str query) })]
    (transit-read (:body resp))))

(comment
  
  (test-server :get "api/players" nil)
  (test-server :get "api/current-tournament" nil)
  (test-server :post "api/start-tournament" (json/generate-string {:tournament/id "1234"}))
  (comment (def registration (query-homeserver `[(app.model.mge-servers/register {:server/game-addr "127.0.0.1:27015"
                                                                                  :server/api-addr ~(str "http://127.0.0.1:" (:port config))})])))
  
  (def id (:registration config))
  
  (query-homeserver `[(app.model.mge-servers/ping {:server/id ~id})])
  (query-homeserver `[{(app.model.session/login
                        {:username "thmorriss@gmail.com", :password "arst"})
                       [:session/valid? :account/name]}])

  (query-homeserver `[{:servers/registered [:server/id :server/last-pinged]}])
  
  t)

@(promise)
