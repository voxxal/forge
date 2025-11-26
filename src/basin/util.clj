(ns basin.util
  (:require
   [clojure.java.io :as io]

   [basin.env :refer [env]])
  (:import
   (org.eclipse.jgit.lib Repository)))

(defn repo-path [repo]
  (str (io/file (:repos_root env) repo)))

(defn commit-msg [repo rev-commit]
  (str (-> rev-commit (.getId) (.getName) (subs 0 7)) ": " (.getShortMessage rev-commit)))

(defn default-branch [repo]
  (-> repo (.getRepository) (.getFullBranch) (Repository/shortenRefName)))
