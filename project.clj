(defproject dojo-appointment-booking-react "0.1.0-SNAPSHOT"
  :description "Appointment booking system"
  :url "http://github.com/dinnymate/dojo-appointment-booking-react"
  :license {:name "MIT"}

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [integrant "0.8.0-alpha2"]
                 [integrant/repl "0.3.1"]
                 [camel-snake-kebab "0.4.0"]

                 [metosin/compojure-api "2.0.0-alpha30"]
                 [metosin/spec-tools "0.10.0"]
                 [http-kit "2.3.0"]
                 [ring/ring-core "1.7.1"]
                 [hiccup "1.0.5"]

                 [thheller/shadow-cljs "2.8.52"]
                 [org.clojure/clojurescript "1.10.520"]
                 [funcool/promesa "3.0.0"]]

  :resource-paths ["resources" "target/js"]
  :target-path "target/lein/%s"
  :clean-targets ["target"]
  :prep-tasks ["javac" "compile"
               ["run" "-m" "shadow.cljs.devtools.cli" "compile" "app"]]
  :main ^:skip-aot dojo-appointment.server.core

  :profiles {:repl {:prep-tasks ^:replace ["javac" "compile"]}
             :dev {:source-paths ["dev"]}
             :uberjar {:aot :all
                       :prep-tasks ^:replace ["javac" "compile"
                                              ["run" "-m" "shadow.cljs.devtools.cli" "release" "app"]]}}

  :repl-options {:init-ns user
                 :nrepl-middleware [shadow.cljs.devtools.server.nrepl04/middleware]})
