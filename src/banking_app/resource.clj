(ns banking-app.resource
  (:require yada.resource))

(def model-defaults
  {:produces [{:media-type #{"application/json"}
               :charset    "UTF-8"}]
   :consumes [{:media-type #{"application/json"}
               :charset    "UTF-8"}]
   :responses
   {;; catch-all exception responses to not spill stack trace to client
    *
    (let [msg "Unknown error."]
      {:description msg
       :produces    "application/json"
       :response
       (fn [ctx]
         {:error msg})})}

   :access-control
   {:allow-methods     #{:delete :get :head :options :post :put}}})


(defn create-resource [model]
  (->> model
       (merge model-defaults)
       yada.resource/resource))
