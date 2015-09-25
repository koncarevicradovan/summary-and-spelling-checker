(ns summary-and-spelling-checker.routes.summary
  (:require [compojure.core :refer :all]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.route :refer [restricted]]
            [clojure.string :refer (join)]
            [summary-and-spelling-checker.views.layout :as layout]
            [summary-and-spelling-checker.models.summary-extractor :refer [get-thesis]]
            [summary-and-spelling-checker.models.database :as db]))

; handling routes

(defn handle-summary-page []
  (layout/render "summarypage.html"))

(defn handle-get-summary [text1 text2 text3 numberOfThessis]
  (resp/json {:resp (join "\n- "(get-thesis text1 text2 text3 numberOfThessis))}))

(defn handle-save-text-summary [text summary]
  (if (db/insert-text-summary (session/get :user) text summary)
    (resp/json {:Ok "true"})
    (resp/json {:Ok "false"})))

(defroutes summary-routes
  (GET "/summary" [] 
       (restricted (handle-summary-page)))
  (POST "/getsummary" [text1 text2 text3 numberOfThessis] 
        (restricted (handle-get-summary text1 text2 text3 numberOfThessis)))
  (POST "/save_text_summary" [text summary] 
        (restricted (handle-save-text-summary text summary))))