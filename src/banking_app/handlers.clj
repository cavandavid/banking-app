(ns banking-app.handlers
  (:require [banking-app.resource :refer [create-resource]]
            [banking-app.db :as db]
            [schema.core :as s]))

(def create-account
  (create-resource
   {:methods
    {:post
     {:parameters {:body {:name s/Str}}
      :response
      (fn [ctx]
        (let [{:accounts/keys [id name balance]}
              (db/insert-data :accounts
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
              (first (db/query-data :accounts
                                    {:id (get-in
                                          ctx
                                          [:parameters :path :id])}))]
          (if id
            {:account-number id
             :name name
             :balance balance}
            (assoc (:response ctx) :status 404))))}}}))

(defn deposit-withdraw
  "Handler for managing deposits OR withdrawals for an account"
  [operation]
  (create-resource
   {:methods
    {:post
     {:parameters {:body {:amount s/Num}
                   :path {:id s/Int}}
      :response
      (fn [ctx]
        (let [{:accounts/keys [id name balance] :as result}
              (db/deposit-withdraw operation
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

(def transfer-money
  "Transfers money from one account to another"
  (create-resource
   {:methods
    {:post
     {:parameters {:body {:amount s/Num
                          :account-number s/Int}
                   :path {:id s/Int}}
      :response
      (fn [ctx]
        ;; TODO: Can add check to ensure you cant transfer money to yourself
        (let [{:keys [amount account-number]} (get-in ctx [:parameters :body])
              {:accounts/keys [id name balance] :as result}
              (db/transfer-money amount
                                 (get-in ctx [:parameters :path :id])
                                 account-number)]
          (cond
            id
            {:account-number id
             :name name
             :balance balance}
            (= result :insufficient-balance)
            (assoc (:response ctx) :status 409)
            :else
            (assoc (:response ctx) :status 404))))}}}))
