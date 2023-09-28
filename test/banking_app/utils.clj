(ns banking-app.utils
  (:require [yada.handler :refer [as-handler]]
            [cheshire.core :as json]
            [byte-streams :as b]))

(defn ->json-bytes
  "Convert `m` to a JSON byte-buffer, returning it and the size of the encoded
  string."
  [m]
  (when m
    (let [j (json/encode m)
          c (count j)
          b (b/to-byte-buffers j)]
      [b c])))

(defn request-for
  "Variant of yada.test/request-for that marshals any body to JSON and sets content-type and
  content-length headers."
  [method uri options]
  (let [uri                   (new java.net.URI uri)
        body                  (:body options)
        [json-body body-size] (->json-bytes body)]
    (merge
     {:server-port    80
      :server-name    "localhost"
      :remote-addr    "localhost"
      :uri            (.getPath uri)
      :query-string   (.getRawQuery uri)
      :scheme         :http
      :request-method method}
     (cond-> options
       body (assoc :body json-body)
       body (update :headers #(merge {"content-type"   "application/json"
                                      "content-length" (str body-size)} %))))))

(defn response-for
  "Variant of yada.test/response-for that uses custom `request-for` and unmarshals JSON body"
  ([o]
   (response-for o :get "/" {}))
  ([o method]
   (response-for o method "/" {}))
  ([o method uri]
   (response-for o method uri {}))
  ([o method uri options]
   (let [h (as-handler o)
         response @(h (request-for method uri options))]
     (cond-> response
       (:body response) (update :body (comp json/decode b/to-string))))))
