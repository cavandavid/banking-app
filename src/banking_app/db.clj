(ns banking-app.db
  (:require [migratus.core :as m]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.types :as jdbc.types]
            [honey.sql :as hsql]
            [next.jdbc.connection :as connection])
  (:import [com.mchange.v2.c3p0 ComboPooledDataSource]))

(def db-config
  "FIXME: To be sourced from config file"
  {:dbtype   "postgresql"
   :dbname   "postgres"
   :user     "postgres"
   :password "mysecretpassword"
   :host     "localhost"
   :port     5432})

(def db-pool
  " DB pool to speed up multiple db operations without overhead of setting up connection each time
  FIXME: Initialize it via structured booting like integrant later"
  (connection/->pool ComboPooledDataSource db-config))

(defn run-migrations
  "Initiliazes the empty tables needed for banking application"
  []
  (let [default-settings {:store :database
                          :migration-dir "migrations"
                          :migration-table-name "bank_migrations"}]
    (m/migrate (assoc default-settings :db db-config))))

(defn insert-data
  "Generic insert data utility function"
  [table record-data]
  (sql/insert! db-pool table record-data))

(defn insufficient-balance-error?
  "Returns truthy value if exception is about insufficient balance
  else throws exception"
  [ex]
  (if-not (clojure.string/includes? (ex-message ex)
                                    "violates check constraint \"balance_nonnegative\"")
    (throw ex)
    :insufficient-balance))

(defn query-data
  "Generic utility function to query data from a table based on where condition"
  [table where-details]
  (sql/find-by-keys db-pool table where-details))

(defn deposit-withdraw
  "Function to deposit or withdraw money for a given account"
  [operator amount id]
  (try (jdbc/with-transaction [tx db-pool]
         (let [response (jdbc/execute!
                         tx
                         (hsql/format
                          {:update :accounts
                           :set {:balance [operator :balance amount]},
                           :where [:= :id id]})
                         {:return-keys true})]
            (sql/insert! tx :transactions
                         {:account_id id :amount amount
                          :type (jdbc.types/as-other (if (= operator :+)
                                                       "credit" "debit"))})
           (first response)))
       (catch org.postgresql.util.PSQLException ex
         (insufficient-balance-error? ex))))

(defn transfer-money
  "Transfers money between two accounts"
  [amount from to]
  (try
    (jdbc/with-transaction [tx db-pool]
      (when-let [senders-balance (first
                                  (jdbc/execute! tx (hsql/format
                                                     {:update :accounts
                                                      :set {:balance [:- :balance amount]},
                                                      :where [:= :id from]})
                                                 {:return-keys true}))]
        (jdbc/execute! tx (hsql/format
                           {:update :accounts
                            :set {:balance [:+ :balance amount]},
                            :where [:= :id to]}))
        (sql/insert! tx :transactions
                         {:account_id from :amount amount
                          :type (jdbc.types/as-other "transfer")
                          :recipient_id to})
        senders-balance))
    (catch org.postgresql.util.PSQLException ex
      (insufficient-balance-error? ex))))

(defn get-history
  "Either returns all or last n transaction records for given account id"
  [account-id & [last-n]]
  (let [response (jdbc/execute! db-pool
                                (hsql/format
                                 (cond-> {:select [:account_id :type :amount :recipient_id
                                                   [[:raw ["ROW_NUMBER() OVER (ORDER BY created_at) - 1 AS sequence"]]]]
                                          :from   [[:transactions :t]]
                                          :where  [:or [:= :t.account_id account-id]
                                                   [:= :t.recipient_id account-id]]
                                          :order-by [[:t.created_at :desc]]}
                                   last-n (merge {:limit last-n}))))]
    (mapv (fn [{:transactions/keys [account_id type amount recipient_id] :as record}]
            (cond-> {:sequence (:sequence record)
                     :description  (case type
                                     "debit" "withdraw"
                                     "credit" "deposit"
                                     "transfer" (if (= recipient_id account-id)
                                                  (str "recieve from #" account_id)
                                                  (str "send to #" recipient_id)))}
              (and (= type "transfer") (= recipient_id account-id)) (assoc :credit amount)
              (#{"transfer" "debit"} type) (assoc  :debit amount)
              (= type "credit") (assoc  :credit amount)))
          response)))

(comment
;; CRUD With Money
  (insert-data :accounts {:name "Mr Cavan"})
  (insert-data :accounts {:name "Mr John"})
  (query-data :accounts {:id 1})
  (deposit-withdraw :+ 200 1)
  (transfer-money 200 1 2)
;; Create a Migration
  (def migration-dir "migrations")
  (def migration-name "initialize-banking-tables")
  (m/create {:migration-dir migration-dir} migration-name)
  )
