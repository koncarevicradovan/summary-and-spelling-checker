(ns summary-and-spelling-checker.models.database  
  (:require [monger.core :as mg]
            [monger.operators :refer :all]
            [monger.collection :as mc])
  
  (:import (org.bson.types ObjectId)))

(def connection (mg/connect))

(def db (mg/get-db connection "summary_and_spelling_checker_db"))

(defn insert-initial-users []
  (mc/insert db "users" {:_id (str (ObjectId.)) :first_name "Radovan" :last_name "Koncarevic" :email "koncarevicradovan@gmail.com" :username "usr" :password "pwd" :isAdminUser "false"})
  (mc/insert db "users" {:_id (str (ObjectId.)) :first_name "admin" :last_name "admin" :email "admin@gmail.com" :username "admin" :password "admin" :isAdminUser "true"}))

(defn get-text-summary-by-username [usr]
  (mc/find-maps db "textsummary" {:username usr}))

(defn insert-new-user [usr pwd first-name last-name email isAdmin]
  (mc/insert db "users" {:_id (str (ObjectId.)) :first_name first-name :last_name last-name :email email :username usr :password pwd :isAdminUser isAdmin}))

(defn return-all-users []
  (mc/find-maps db "users"))

(defn return-user-by-username [usr]
  (mc/find-one-as-map db "users" {:username usr}))

(defn update-user-by-username [usr first-name last-name email]
  (mc/update db "users" {:username usr} {$set {:first_name first-name :last_name last-name :email email}}))

(defn update-user-by-username-new-pwd [usr first-name last-name email newpwd]
  (mc/update db "users" {:username usr} {$set {:first_name first-name :last_name last-name :email email :password newpwd}}))


(defn delete-user-by-username [usr]
  (mc/remove db "users" {:username usr}))

(defn username-not-exists [usr]
  (let [number-of-users (mc/find-maps db "users" {:username usr})]
    (= (count number-of-users) 0)))

(defn is-user-admin [usr]
  (let [number-of-users (mc/find-maps db "users" {:username usr :isAdminUser "true"})]
    (= (count number-of-users) 1)))

(defn is-user-valid [usr pwd]
  (let [number-of-users (mc/find-maps db "users" {:username usr :password pwd})]
    (= (count number-of-users) 1)))

(defn insert-text-summary [usr text summary]
  (mc/insert db "textsummary" {:_id (str (ObjectId.)) :username usr :text text :summary summary}))

(defn init-database []
  (if (empty? (return-all-users))
    (insert-initial-users)))