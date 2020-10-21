(ns survey.coeffects
  (:require
   [clojure.string :as str]
   [re-frame.core :as re-frame]))

(defn parse-params
  "Parse URL parameters into a hashmap"
  []
  (let [param-strs (-> (.-location js/window)
                       (str/split #"\?")
                       last
                       (str/split #"\&"))]
    (into {} (for [[k v] (map #(str/split % #"=") param-strs)]
               [(keyword k) v]))))

(re-frame/reg-cofx
 ::query-params
 (fn [cofx _]
   (assoc cofx :query-params (parse-params))))

