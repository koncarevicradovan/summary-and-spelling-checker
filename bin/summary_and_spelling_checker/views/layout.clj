(ns summary-and-spelling-checker.views.layout
  (:require [selmer.parser :as parser]
            [selmer.filters :as filters]
            [noir.session :as session]
            [markdown.core :refer [md-to-html-string]]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(declare ^:dynamic *servlet-context*)
(parser/set-resource-path!  (clojure.java.io/resource "html_templates"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :markdown (fn [content] [:safe (md-to-html-string content)]))

(defn render [template & [params]]
  (-> template
      (parser/render-file
        (assoc params
          :page template
          :csrf-token *anti-forgery-token*
          :servlet-context *servlet-context*
          :user (session/get :user)))
      response
      (content-type "text/html; charset=utf-8")))