(ns banking-app.db
  (:require [migratus.core :as m]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [honey.sql :as hsql]))

(def db-config
  "FIXME: To be sourced from config file"
  {:dbtype   "postgresql"
   :dbname   "postgres"
   :user     "postgres"
   :password "mysecretpassword"
   :host     "localhost"
   :port     5432})

(defn run-migrations
  "Initiliazes the empty tables needed for banking application"
  [db-config]
  (let [default-settings {:store :database
                          :migration-dir "migrations"
                          :migration-table-name "bank_migrations"}]
    (m/migrate (assoc default-settings :db db-config))))

(defn insert-data
  "Generic insert data utility function"
  [table record-data]
  (with-open [connection (jdbc/get-connection db-config)]
    (sql/insert! connection table record-data)))

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
  (with-open [connection (jdbc/get-connection db-config)]
    (sql/find-by-keys connection table where-details)))

(defn deposit-withdraw
  "Function to deposit or withdraw money for a given account"
  [operator amount id]
  (try (first (with-open [connection (jdbc/get-connection db-config)]
                (jdbc/execute! connection
                               (hsql/format
                                {:update :accounts
                                 :set {:balance [operator :balance amount]},
                                 :where [:= :id id]})
                               {:return-keys true})))
       (catch org.postgresql.util.PSQLException ex
         (insufficient-balance-error? ex))))

(defn transfer-money
  "Transfers money between two accounts"
  [amount from to]
  (try
    (jdbc/with-transaction [tx db-config]
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
        senders-balance))
    (catch org.postgresql.util.PSQLException ex
      (insufficient-balance-error? ex))))

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
