(ns banking-app.core
  (:require [banking-app.handlers :as handlers]
            [banking-app.db :refer [run-migrations]]
            [yada.yada :as yada]
            [yada.aleph :refer [listener]]))


(defonce server (atom nil))

(def routes
  "All routes including Swagger and a 404 handler"
  [""
   [["/account" handlers/create-account]
    [["/account/" :id]
     [["" handlers/view-account]
      ["/deposit" (handlers/deposit-withdraw :+)]
      ["/withdraw" (handlers/deposit-withdraw :-)]
      ["/send" handlers/transfer-money]
      ["/audit" handlers/audit]]]]])

(defn launch-server
  "Launches high performance aleph http server(netty based)"
  [port]
  (when (:close @server)
    ((:close @server)))
  (reset! server (listener routes
                           {:port port})))

(defn init
 "Initilizes the server that listens at port 3000 and applies necessary migrations for banking app"
 []
 (launch-server 3000)
 (run-migrations))

(defn -main
  [& args]
  (init))
 
(comment
  (init)
)
