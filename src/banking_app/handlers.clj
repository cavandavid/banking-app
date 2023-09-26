(ns banking-app.handlers
  (:require [banking-app.resource :refer [create-resource]]
            [banking-app.db :refer [insert-data
                                    query-data
                                    manage-money]]
            [schema.core :as s]))

(def create-account
  (create-resource
   {:methods
    {:post
     {:parameters {:body {:name s/Str}}
      :response
      (fn [ctx]
        (let [{:accounts/keys [id name balance]}
              (insert-data :accounts
                           {:name (get-in
                                   ctx
                                   [:parameters :body :name])})]

          {:account-number id
           :name name
           :balance balance}))}}}))

(def view-account
  (create-resource
   {:methods
    {:get
     {:parameters {:path {:id s/Int}}
      :response
      (fn [ctx]
        (let [{:accounts/keys [id name balance]}
              (first (query-data :accounts
                                 {:id (get-in
                                       ctx
                                       [:parameters :path :id])}))]
          (if id
            {:account-number id
             :name name
             :balance balance}
            (assoc (:response ctx) :status 404))))}}}))

(def deposit
  (create-resource
   {:methods
    {:post
     {:parameters {:body {:amount s/Num}
                   :path {:id s/Int}}
      :response
      (fn [ctx]
        (let [{:accounts/keys [id name balance]}
              (manage-money :+
                            (get-in ctx [:parameters :body :amount])
                            (get-in ctx [:parameters :path :id]))]
          (if id
            {:account-number id
             :name name
             :balance balance}
            (assoc (:response ctx) :status 404))))}}}))

(def withdraw
  (create-resource
   {:methods
    {:post
     {:parameters {:body {:amount s/Num}
                   :path {:id s/Int}}
      :response
      (fn [ctx]
        (let [{:accounts/keys [id name balance] :as result}
              (manage-money :-
                            (get-in ctx [:parameters :body :amount])
                            (get-in ctx [:parameters :path :id]))]
          (cond
            id
            {:account-number id
             :name name
             :balance balance}
            (= result :insufficient-balance)
            (assoc (:response ctx) :status 409)
            :else
            (assoc (:response ctx) :status 404))))}}}))
