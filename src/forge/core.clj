(ns forge.core
  (:require
   [compojure.core :refer :all]
   [compojure.route :as route]
   [ring.util.response :as resp]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.adapter.jetty :refer [run-jetty]]
   [clj-jgit.porcelain :refer :all]

   [forge.util :as util]
   [forge.views :as views]))

(defn create-repo [name]
  (let [dir (util/repo-path name)]
    (git-init {:bare? true, :dir dir})))

(defroutes app-routes
  (GET "/" [] (views/index))
  (GET "/new" [] (views/new-repo))
  ; TODO add validation for repo name
  (POST "/new" {params :params}
        (let [name (params :name)]
          (create-repo name)
          (resp/redirect (str "/r/" name) 303)))
  (GET "/r/:repo" [repo] (views/repo repo))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn -main [] 
  (run-jetty #'app {:port 3000}))
