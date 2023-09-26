(defproject banking-app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [aleph "0.4.6"]
                 [yada "1.2.15"]
                 [com.github.seancorfield/next.jdbc "1.3.894"]
                 [migratus "1.2.8"]
                 [org.slf4j/slf4j-log4j12 "2.0.9"]
                 [org.postgresql/postgresql "42.6.0"]]
  :repl-options {:init-ns banking-app.core})
