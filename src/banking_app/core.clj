(ns banking-app.core
  (:require [banking-app.handlers :as handlers]
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
      ["/send" handlers/transfer-money]]]]])

(defn launch-server
  [port]
  (when (:close @server)
    ((:close @server)))
  (reset! server (listener routes
                           {:port port})))

(comment
  (launch-server 3000)
)
