(ns summary-and-spelling-checker.routes.spellingcorrector
  (:require [compojure.core :refer :all]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.route :refer [restricted]]
            [clojure.string :refer (join)]
            [summary-and-spelling-checker.views.layout :as layout]
            [summary-and-spelling-checker.models.spelling_corrector :refer [correct_word correct_text improve_training_txt]]))

; handling routes

(defn handle-spelling-page []
  (layout/render "spellingcorrectorpage.html"))

(defn handle-correcttext [text]
    (resp/json {:resp (correct_text text)}))

(defn handle-correctword [word]
    (resp/json {:resp (correct_word word)}))

(defn handle-traincorrector [word]
  (improve_training_txt word)
    (resp/json {:Ok "true" }))

(defroutes spelling-routes
  (GET "/spelling" [] 
       (restricted (handle-spelling-page)))
  (POST "/correcttext" [text] 
        (restricted (handle-correcttext text)))
  (POST "/correctword" [word] 
        (restricted (handle-correctword word)))
  (POST "/traincorrector" [word] 
        (restricted (handle-traincorrector word))))
