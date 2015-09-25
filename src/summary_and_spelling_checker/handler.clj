(ns summary-and-spelling-checker.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.util.middleware :as noir-middleware]
            [noir.session :as session]
            [summary-and-spelling-checker.routes.home :refer [home-routes]]
            [summary-and-spelling-checker.routes.authentication :refer [authentication-routes]]
            [summary-and-spelling-checker.routes.summary :refer [summary-routes]]
            [summary-and-spelling-checker.routes.spellingcorrector :refer [spelling-routes]]
            [summary-and-spelling-checker.routes.administration :refer [administration-routes]]
            [summary-and-spelling-checker.routes.user :refer [user-profile-routes]]
            [summary-and-spelling-checker.models.database :refer [init-database]]
            [summary-and-spelling-checker.views.layout :refer [*servlet-context*]]))

(defn wrap-servlet-context [handler]
  (fn [request]
    (binding [*servlet-context*
              (if-let [context (:servlet-context request)]
                (try (.getContextPath context)
                     (catch IllegalArgumentException _ context)))]
      (handler request))))

(defn init []
  (println "summary-and-spelling-checker is starting")
  (println "Initializing database... ")
  (init-database)
  )

(defn destroy []
  (println "summary-and-spelling-checker is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn user-page [_]
  (session/get :user))

(def app
  (-> [authentication-routes home-routes summary-routes spelling-routes administration-routes user-profile-routes app-routes]
      (noir-middleware/app-handler)
      (noir-middleware/wrap-access-rules [user-page])
      (wrap-servlet-context)))