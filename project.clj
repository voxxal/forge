(defproject basin "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.12.2"]
                 [org.clojure/tools.logging "1.3.0"]
                 [ring/ring-core "1.15.3"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.15.3"]
                 [org.slf4j/slf4j-simple "2.0.17"]
                 [com.novemberain/validateur "2.6.0"]
                 [hiccup "2.0.0"]
                 [garden "1.3.10"]
                 [compojure "1.7.2"]
                 [datalevin "0.9.22"]
                 [clj-jgit "1.1.0"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler basin.core/app}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
