(ns forge.util
  (:require
   [clojure.java.io :as io]

   [forge.env :refer [env]]))

(defn repo-path [repo]
  (str (io/file (:repos_root env) repo)))
