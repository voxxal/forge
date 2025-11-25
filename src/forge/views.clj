(ns forge.views
  (:require
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [clj-jgit.internal :refer :all]
   [clj-jgit.porcelain :refer :all]
   [hiccup.page :as page]
   [hiccup.form :as form]
   [garden.core :refer [css]]

   [forge.util :as util]))

(def navbar 
  [:nav
   "[ "
   [:a {:href "/"} [:strong "forge"]]
   " | "
   [:a {:href "/new"} "+ new repo"]
   " ]"])

(defn layout [title & contents]
  (page/html5
   [:head [:title (str "forge | " title)]]
   navbar
   [:body contents]))

(defn index []
  (layout
   "index"
   [:h1 "welcome to forge"]
   [:p "an minimal git forge"]))

(defn new-repo []
  (layout
   "new repo"
   [:h1 "new repo"]
   [:form {:method "POST"}
    (anti-forgery-field)
    (form/text-field
     {:name "name"
      :required true
      :pattern "[a-zA-Z\\-_]+"}
     "repo name")
    (form/submit-button "create repo")]))

(defn get-files [name]
  (with-repo (util/repo-path name)
    (let [rev-commit (get-head-commit repo)
          tree-walk (new-tree-walk repo rev-commit)]
      (loop [files []]
        (if (.next tree-walk)
          (recur (conj files (.getPathString tree-walk)))
          files)))))

(defn repo [name]
  (layout
   name
   [:h1 name]
   (for [file (get-files name)]
     [:p file])))
