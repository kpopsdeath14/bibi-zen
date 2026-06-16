(defproject bibi_zen_back "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[com.taoensso/timbre "5.1.2"]
                 [org.clojure/java.classpath "1.0.0"]
                 [org.clojure/clojure "1.10.1"]
                 [compojure "1.6.2"]
                 [yogthos/config "1.1.7"]
                 [kibu/pushy "0.3.8"]
                 [cljs-ajax "0.8.1"]
                 [bidi "2.1.6"]
                 [http-kit "2.3.0"]
                 [buddy/buddy-core "1.7.1"]
                 [digest "1.4.9"]
                 [metosin/jsonista "0.2.7"]
                 [ring-cors "0.1.13"]
                 [ring/ring-json "0.5.1"]
                 [clj-http "3.13.0"]
                 [clj-time "0.15.2"]
                 [org.clojure/data.codec "0.1.1"]
                 [ring "1.8.1"]
                 [ring/ring-defaults "0.3.2"]
                 [org.postgresql/postgresql "42.7.1"]
                 [com.github.seancorfield/next.jdbc "1.3.981"]
                 [org.clojure/data.json "1.0.0"]
                 [com.draines/postal "2.0.4"]
                 [org.clojure/core.async "1.8.741"]]
  :repl-options {:init-ns bibi-zen-back.core}
  :main bibi-zen-back.core
  :aot  :all

  :jvm-opts ["-Xmx2G"]

  :profiles {:uberjar {:aot :all
                       :uberjar-name "backend.jar"}})
