(defproject clojalk "1.0.0-SNAPSHOT"
  :description "A beanstalkd clone in clojure"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [aleph "0.2.0-beta1"]]
  :dev-dependencies [[org.clojars.sunng/beanstalk "1.0.5"]
                     [lein-exec "0.1"]]
  :main clojalk.main)
