(ns control
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str]
            [hiccup2.core :refer [html]]
            [org.httpkit.server :as server]
            [cheshire.core :as json]
            [taoensso.timbre :as log]
            [org.httpkit.client :as client]))


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
           {:body (json/generate-string {:epic "win"})}
           
           
           [:get ["users" id]]
           {:body (str (html [:div id]))}
           
           :else {:body (str (html [:html "Welcome!"]))})))



(def config (clojure.edn/read-string (slurp "config.edn")))

(log/info "starting server with config" (pr-str config))

(defonce server (server/run-server router (or {:port 8091}
                                           (select-keys config [:port]))))

(defn test-server [method url body]
  (json/parse-string (:body @(client/request (cond-> {:method method
                                    :url (str "http://0.0.0.0:8091/" url)
                                    :as :text}
                             body (assoc :body body))))
                     keyword))

(comment
  (test-server :get "api/" nil))

@(promise)
