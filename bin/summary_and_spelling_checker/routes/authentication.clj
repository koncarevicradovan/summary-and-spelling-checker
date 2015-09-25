(ns summary-and-spelling-checker.routes.authentication
  (:require [compojure.core :refer :all]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.route :refer [restricted]]
            [noir.validation :refer [rule has-value? min-length? is-email? errors? on-error get-errors]]
            [clojure.java.io :as io]
            [summary-and-spelling-checker.models.database :as db]
            [summary-and-spelling-checker.views.layout :as layout]))

;helpers

(defn is-user-valid [usr pwd pwd1 first-name last-name email]
  (rule (has-value? usr)
        [:usr "Username is required field !"])
  (rule (db/username-not-exists usr)
        [:usr "Username is already used !"])
  (rule (has-value? first-name)
        [:first-name "First name is required field !"])
  (rule (has-value? last-name)
        [:last-name "Last name is required field !"])
  (rule (has-value? email)
        [:email "E-mail is required field !"])
  (rule (is-email? email)
        [:email "E-mail format is not the valid !"])
  (rule (min-length? pwd 6)
        [:pwd "Password must be at least 6 characters !"])
  (rule (= pwd pwd1)
        [:pwd "Passwords do not match !"])
  (not (errors? :usr :pwd :pwd1 :first-name :last-name :email)))

(defn redirect-to [usr]
  (if (db/is-user-admin usr)
    (resp/redirect "/administration")
    (resp/redirect "/home")))

; handling routes

(defn handle-login-page []
  (if (session/get :user)
    (session/clear!))
  (layout/render "login.html"))

(defn handle-login [usr pwd]
  (if (db/is-user-valid usr pwd)
    (do
      (session/put! :user usr)
      (redirect-to usr))
    (resp/redirect "/")))

(defn handle-registration-page [& [usr first-name last-name email]]
  (layout/render "signup.html" 
                 {:usr usr
                  :first-name first-name
                  :last-name last-name
                  :email email
                  :usr-error (first (get-errors :usr))
                  :first-name-error (first (get-errors :first-name))
                  :last-name-error (first (get-errors :last-name))
                  :email-error (first (get-errors :email))
                  :pwd-error (first (get-errors :pwd))}))

(defn handle-logout []
  (session/clear!)
  (resp/redirect "/"))

(defn handle-registration [usr pwd pwd1 first-name last-name email]
  (if (is-user-valid usr pwd pwd1 first-name last-name email)
    (do
      (db/insert-new-user usr pwd first-name last-name email "false")
      (session/put! :user usr)
      (resp/redirect "/home"))
    (handle-registration-page usr first-name last-name email)))

(defroutes authentication-routes
  (GET "/" [] 
       (handle-login-page))
  (POST "/login" [usr pwd]
        (handle-login usr pwd))
  (GET "/register" []
       (handle-registration-page))
  (GET "/logout" []
       (restricted (handle-logout)))
  (POST "/register" [usr pwd pwd1 first-name last-name email]
        (handle-registration usr pwd pwd1 first-name last-name email)))