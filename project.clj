(defproject video-rental-store "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["clojure/src"]
  :java-source-paths ["java/src"]
  :main video-rental-store.server

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [bidi "2.0.9"]
                 [liberator "0.14.1"]
                 [munge-tout "0.1.4"]
                 [midje "1.8.3"]]
  )



