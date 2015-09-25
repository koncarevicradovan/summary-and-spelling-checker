(ns summary-and-spelling-checker.routes.home
    (:require [compojure.core :refer :all]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.route :refer [restricted]]
            [summary-and-spelling-checker.views.layout :as layout]
            [summary-and-spelling-checker.models.database :as db]))

(defn home-page []
  (layout/render
    "home.html"))

(defroutes home-routes
  (GET "/home" []
       (restricted(home-page))))