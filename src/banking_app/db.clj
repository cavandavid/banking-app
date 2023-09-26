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

(defn query-data
  "Generic utility function to query data from a table based on where condition"
  [table where-details]
  (println where-details)
  (with-open [connection (jdbc/get-connection db-config)]
    (sql/find-by-keys connection table where-details)))

(defn manage-money
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
         (if-not (clojure.string/includes? (ex-message ex)
                                           "violates check constraint")
           (throw ex)
           :insufficient-balance))))

(comment
  ;; CRUD With Money
  (insert-data :accounts {:name "Mr Cavan"})
  (query-data :accounts {:id 1})
  (manage-money :- 200 1)
;; Create a Migration
  (def migration-dir "migrations")
  (def migration-name "initialize-banking-tables")
  (m/create {:migration-dir migration-dir} migration-name))
