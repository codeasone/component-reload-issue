(defproject component-ring-issue "0.1.0-SNAPSHOT"
  :description "Demonstrates component / lein-ring interop issue"
  :url "https://github.com/codeasone/component-ring-issue"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]]

  :main ^:skip-aot component-ring-issue.core

  :test-selectors {:default (complement :integration)
                   :integration :integration }

  :profiles
  {:dev {:plugins [[lein-eftest "0.3.1"]]}

   :uberjar {:aot :all}})
