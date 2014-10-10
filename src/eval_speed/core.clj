(ns eval-speed.core
  (:require
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]))

(defn rewrite-csv []
  (with-open [out-file (io/writer "resources/stuff.csv")]
    (csv/write-csv out-file
      (for [i (range 100000)]
        (for [j (range 10)]
          (str (java.util.UUID/randomUUID)))))))

(defonce csv-fix
  (with-open [in-file (-> "stuff.csv" io/resource io/reader)]
    (->>
      in-file
      csv/read-csv
      doall)))

(defn read-to-maps [rows]
  (let [headers (->>
                  rows
                  first
                  (take-while (complement #{""}))
                  (map keyword))]
    (for [row (rest rows)]
      (zipmap headers row))))
 
 
(defn read-to-maps-eval [rows]
  (let [headers (->>
                  rows
                  first
                  (take-while (complement #{""}))
                  (map keyword))
        names (map (comp symbol name) headers)
        mapper (eval
                 `(fn [[~@names]]
                    ~(zipmap headers names)))]
    (map mapper (rest rows))))

(defn time-fn [f & [n]]
  (time
    (dotimes [i (or n 20)]
      (dorun (f csv-fix)))))

(comment
  (time-fn read-to-maps)
  (time-fn read-to-maps-eval))
