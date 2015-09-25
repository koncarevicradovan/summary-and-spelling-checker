(ns summary-and-spelling-checker.routes.administration
  (:require [compojure.core :refer :all]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.route :refer [restricted]]
            [summary-and-spelling-checker.views.layout :as layout]
            [summary-and-spelling-checker.models.database :as db]))

; handling routes

(defn handle-administration-page []
  (layout/render "administration.html"))

(defn handle-get-user [usr]
  (resp/json (db/return-user-by-username usr)))

(defn handle-update-user [usr first-name last-name email]
  (if (db/update-user-by-username usr first-name last-name email)
    (resp/json {:Ok "true"})
    (resp/json {:Ok "false"})))

(defn handle-get-users []
  (resp/json (db/return-all-users)))

(defn handle-create-user [usr first-name last-name email pwd isAdmin]
  (if (db/insert-new-user usr pwd first-name last-name email isAdmin)
    (resp/json {:Ok "true"})
    (resp/json {:Ok "false"})))

(defn handle-delete-user [usr]
  (if (db/delete-user-by-username usr)
    (resp/json {:Ok "true"})
    (resp/json {:Ok "false"})))

(defroutes administration-routes
  (GET "/administration" [] 
       (restricted (handle-administration-page)))
  (GET "/user" [usr]
       (restricted (handle-get-user usr)))
  (POST "/user" [usr first_name last_name email]
       (restricted (handle-update-user usr first_name last_name email)))
  (GET "/users" []
       (restricted (handle-get-users)))
  (POST "/create_user" [usr first_name last_name email pwd isAdmin]
       (restricted (handle-create-user usr first_name last_name email pwd isAdmin)))
  (DELETE "/user" [usr]
          (restricted(handle-delete-user usr))))