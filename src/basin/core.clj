(ns basin.core
  (:require
   [compojure.core :refer :all]
   [compojure.route :as route]
   [ring.util.response :as resp]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.adapter.jetty :refer [run-jetty]]
   [clj-jgit.porcelain :refer :all]

   [basin.util :as util]
   [basin.views :as views]))

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
          (resp/redirect (str "/" name) 303)))
  (context "/:repo" [repo]
           (GET "/" [] (views/repo repo))
           (GET "/log/:ref" [ref] (views/repo-log repo ref))
           (GET "/tree/:ref" [ref] (views/repo-tree repo ref))
           (GET "/tree/:ref/:path{.*}", [ref path]
                (if (empty? path)
                  (resp/redirect (str "/" repo "/tree/" ref)) 
                  (views/repo-file repo ref path))))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn -main [] 
  (run-jetty #'app {:port 3000}))
