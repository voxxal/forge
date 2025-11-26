(ns basin.views
  (:require
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [clj-jgit.querying :refer :all]
   [clj-jgit.internal :refer :all]
   [clj-jgit.porcelain :refer :all]
   [hiccup.page :as page]
   [hiccup.form :as form]
   [garden.core :refer [css]]

   [basin.env :refer [env]]
   [basin.util :as util]))

(def navbar 
  [:nav
   "[ "
   [:a {:href "/"} [:strong "basin"]]
   " | "
   [:a {:href "/new"} "+ new repo"]
   " ]"])

(defn layout [title & contents]
  (page/html5
   [:head [:title (str "basin | " title)]]
   navbar
   [:body contents]))

(defn index []
  (layout
   "index"
   [:h1 "welcome to basin"]
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

(defn get-tree
  ([repo rev-commit] (get-tree {:recursive false} repo rev-commit))
  ([opts? repo rev-commit] (if rev-commit
     (let [tree-walk (doto (new-tree-walk repo rev-commit) (.setRecursive (:recursive opts?)))]
       (loop [files []]
         (if (.next tree-walk)
           (recur (conj files (.getPathString tree-walk)))
           files)))
     [])))

(defn repo [name]
  (with-repo (util/repo-path name)
    (let [rev-commit (try (get-head-commit repo) (catch Exception e nil))
          tree (get-tree repo rev-commit)]
    (layout
     name
     [:h1 name]
     (if (empty? tree)
       [:div
        [:p "no commits yet"]
        [:h2 "create a new repository"]
        [:pre (str "echo \"# " name "\" >> README.md
git init
git add README.md
git commit -m \"initial commit\"
git branch -M main
git remote add origin " (:base_url env) "/r/" name "
git push -u origin main")]
        [:h2 "push existing repository"]
        [:pre (str "git remote add origin "(:base_url env) "/r/" name "
git branch -M main
git push -u origin main")]]
       [:div
        [:a {:href (str "/" name "/log/" (util/default-branch repo))} (util/commit-msg repo rev-commit)]
        [:h2 "tree"]
        (for [file tree]
          [:p file])])))))

(defn repo-log [name ref]
  (with-repo (util/repo-path name)
    (let [log (git-log repo {:until ref})]
      (layout
       (str name " - " ref " log")
       [:h1 name]
       [:h2 (str "log for " ref)]
       [:div
        (for [rev log]
          [:p
           (util/commit-msg repo (rev :id))
           [:br]
           (str "authored by " (get-in rev [:author :name]))
           [:br]
           (str "commited by " (get-in rev [:committer :name]))])]))))

(defn repo-tree [name ref] (layout "" [:h1 "TODO"]))

(defn repo-file [name ref path]
  (with-repo (util/repo-path name)
    (let [commit (find-rev-commit repo rev-walk ref)
          blob-id (get-blob-id repo commit path)]
      (when blob-id
        (let [content
              (-> repo
                  (.getRepository)
                  (.open blob-id)
                  (.getBytes)
                  (String.)
                  )]
          (layout
           (str name " - " path)
           [:h1 name]
           [:h2 path]
           [:pre content]))))))
