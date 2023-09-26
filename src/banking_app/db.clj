(ns banking-app.db
  (:require [migratus.core :as m]
            [next.jdbc :as jdbc]))

(def db-config
  "FIXME: To be sourced from config file"
  {:dbtype   "postgresql"
   :dbname   "postgres"
   :user     "postgres"
   :password "mysecretpassword"
   :host     "localhost"
   :port     5432})


(defn run-migrations [db-config]
  (let [default-settings {:store :database
                          :migration-dir "migrations"
                          :migration-table-name "bank_migrations"}]
  (m/migrate (assoc default-settings :db db-config))))


(comment
  
  ;;Create a Migration
  (def migration-dir "migrations")
  (def migration-name "initialize-banking-tables")
  (m/create {:migration-dir migration-dir} migration-name)
  )
