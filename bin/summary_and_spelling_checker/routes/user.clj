(ns summary-and-spelling-checker.routes.user
  (:require [compojure.core :refer :all]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.route :refer [restricted]]
            [summary-and-spelling-checker.views.layout :as layout]
            [summary-and-spelling-checker.models.database :as db]))

; handling routes

(defn handle-profile-page []
  (layout/render "profilepage.html"))

(defn handle-get-user []
  (resp/json (db/return-user-by-username (session/get :user))))

(defn handle-update-user [usr first-name last-name email]
  (if (db/update-user-by-username usr first-name last-name email)
    (resp/json {:Ok "true"})
    (resp/json {:Ok "false"})))

(defn handle-update-user-new-pwd [usr first-name last-name email newpwd]
  (if (db/update-user-by-username-new-pwd usr first-name last-name email newpwd)
    (resp/json {:Ok "true"})
    (resp/json {:Ok "false"})))

(defn handle-get-text-summary []
  (resp/json (db/get-text-summary-by-username (session/get :user))))

(defroutes user-profile-routes
  (GET "/profile" []
       (restricted (handle-profile-page)))
  (GET "/user_profile" []
       (restricted (handle-get-user)))
  (POST "/edit_profile" [usr first_name last_name email]
        (restricted (handle-update-user usr first_name last_name email)))
  (POST "/edit_profile_new_pwd" [usr first_name last_name email newpwd]
        (restricted (handle-update-user-new-pwd usr first_name last_name email newpwd)))
  (GET "/get_text_summary" []
        (restricted (handle-get-text-summary))))