(defproject summary-and-spelling-checker "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-server "0.3.1"]
                 [com.novemberain/monger "2.0.0"]
                 [selmer "0.8.2"]
                 [lib-noir "0.7.6"]
                 [markdown-clj "0.9.67"]
                 [incanter/incanter-core "1.5.6"]
                 [net.mikera/core.matrix "0.34.0"]]
  :plugins [[lein-ring "0.8.12"]]
  :ring {:handler summary-and-spelling-checker.handler/app
         :init summary-and-spelling-checker.handler/init
         :destroy summary-and-spelling-checker.handler/destroy}
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.1"]]}}
  :main summary-and-spelling-checker.repl)
