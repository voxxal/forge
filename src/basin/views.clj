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
   [basin.util :as util])
  (:import
   (org.eclipse.jgit.treewalk TreeWalk)
   (org.eclipse.jgit.lib Constants)))

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
  ([tw] (get-tree {:recursive false} tw))
  ([opts? tw]
   (let [tw (doto tw (.setRecursive (opts? :recursive)))]
     (loop [files []]
       (if (.next tw)
         (recur (conj files (.getPathString tw)))
         files)))))

(defn repo [name]
  (with-repo (util/repo-path name)
    (let [rev-commit (try (get-head-commit repo) (catch Exception e nil))]
    (layout
     name
     [:h1 name]
     (if (not rev-commit)
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
       (let [tree (get-tree (new-tree-walk repo rev-commit))
             default-branch (util/default-branch repo)]
         [:div
          [:a {:href (str "/" name "/log/" default-branch)} (util/commit-msg repo rev-commit)]
          [:h2 "tree"]
          (for [file tree]
            [:div
             [:a {:href (str "/" name "/tree/" default-branch "/" file)} file]])]))))))

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
        (let [blob
              (-> repo
                  (.getRepository)
                  (.open blob-id))]
          (layout
           (str name " - " path)
           [:h1 name]
           [:h2 path]
           (if (= (.getType blob) (Constants/OBJ_TREE))
             (let [default-branch (util/default-branch repo)]
               [:div
                (for [file (get-tree (doto (TreeWalk. (.getRepository repo)) (.addTree blob-id)))]
                  [:div
                   [:a {:href (str "/" name "/tree/" default-branch "/" path "/" file)} file]])])
             [:div
               [:pre (String. (.getBytes blob))]])))))))
