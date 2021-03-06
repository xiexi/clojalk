(ns bench
  (:refer-clojure :exclude [use peek read])
  (:use [beanstalk.core]))

(defn sleep [timemillis]
  (Thread/sleep timemillis))

(defn byte-length [s]
  (alength (.getBytes s "utf8")))

(defn make-conn []
  (new-beanstalk 12026))

(def task-body
  "<request><name>Activation</name><email>sunng@sunng.info</email></request>")

(defn do-put [conn]
  (put conn 
       (rand-int 5000)  ; priority
       (rand-int 5) ; delay
       1000 ; ttr 
       (byte-length task-body) 
       task-body))

(defn do-reserve [conn]
  (:id (reserve conn)))

(defn do-delete [conn id]
  (delete conn id))

(def *tube-name* "bench-tube")
(def *puts* (atom 0))
(def *reserves* (atom 0))
(def *deletes* (atom 0))

(defn producer []
  (println "starting producer")
  (let [conn (make-conn)]
    (use conn *tube-name*)
    (loop []
      (do-put conn)
      (swap! *puts* inc)
      (sleep (rand-int 30))
      (recur))))

(defn worker []
  (println "starting worker")
  (let [conn (make-conn)]
    (watch conn *tube-name*)
    (loop []
      (let [id (do-reserve conn)]
        (swap! *reserves* inc)
        (sleep (rand-int 100))
        (do-delete conn id)
        (swap! *deletes* inc)
        (recur)))))

(defn monitor []
  (println "starting monitor")
  (let [conn (make-conn)]
    (use conn *tube-name*)
    (loop []
      (println (:stats (stats-tube conn *tube-name*)))
      (println (str "puts: " @*puts* " reserves: " @*reserves* " deletes: " @*deletes*))
      (sleep 5000)
      (recur))))

(defn run-in-thread [runnable]
  (let [t (Thread. runnable)]
    (.setDaemon t false)
    (.start t)))

(dorun (map run-in-thread (take 5 (repeat producer))))
(dorun (map run-in-thread (take 10 (repeat worker))))
(run-in-thread monitor)
(println "benchmark started")

