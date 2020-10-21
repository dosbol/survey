(ns survey.events
  (:require
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [clojure.edn :as edn]
   [survey.coeffects :as cofx]))

(re-frame/reg-event-db
 ::get-questions-success
 (fn [db [_ result]]
   (assoc db :questions result)))

(re-frame/reg-event-db
 ::get-questions-failure
 (fn [db [_ result]]
   (assoc db :failure result)))

(re-frame/reg-event-fx
 ::initialize
 [(re-frame/inject-cofx ::cofx/query-params)]
 (fn [cofx [_]]
   (let [customer (-> cofx :query-params :customer)
         survey (-> cofx :query-params :survey)]
     {:db {:customer customer
           :survey survey}
      :http-xhrio {:method          :get
                   :uri             survey
                   :response-format {:read (fn [^goog.net.Xhrio res] (-> res .getResponse clojure.edn/read-string))
                                     :content-type    ["text/plain"]}
                   :on-success      [::get-questions-success]
                   :on-failure      [::get-questions-failure]}})))

(re-frame/reg-event-db
 ::answer
 (fn [db [_ id value]]
   (assoc-in db [:answers id] value)))

(re-frame/reg-event-db
 ::submit
 (fn [db _]
   (assoc db :submited? true)))

(re-frame/reg-event-db
 ::unsubmit
 (fn [db _]
   (assoc db :submited? false)))
