(defproject weibo-storm "0.0.1-SNAPSHOT"
  :source-path "src/clj"
  :java-source-path "src"
  :javac-options {:debug "true" :fork "true"}
  :resources-path "res"
  :aot :all
  :jvm-opts ["-Djava.library.path=/usr/local/lib:/opt/local/lib:/usr/lib"]
  :repositories {
                 "clojars.org" "http://clojars.org/repo"
                 "maven" "http://maven.apache.org/maven2" 
                 }

  :dependencies [
		[org.mongodb/mongo-java-driver "2.7.3" ]
  		[mysql/mysql-connector-java "5.1.15"]
  		[c3p0/c3p0 "0.9.1.2"]
  		[com.googlecode.json-simple/json-simple "1.1"]
  		[storm/storm-kestrel "0.7.2-SNAPSHOT"]					

                 ]

  :dev-dependencies [[storm "0.7.4"]
                     [org.clojure/clojure "1.4.0"]
                     ])
