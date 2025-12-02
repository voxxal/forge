(ns basin.env
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [clojure.tools.logging :as log]))

(def env
  (try
    (with-open [r (io/reader "env.edn")]
      (edn/read (java.io.PushbackReader. r)))

    (catch java.io.IOException e
      (log/error "Couldn't open 'env.edn': %s\n" (.getMessage e)))
    (catch RuntimeException e
      (log/error "Error parsing edn file 'env.edn': %s\n" (.getMessage e)))))
