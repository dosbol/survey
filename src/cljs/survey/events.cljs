(ns survey.events
  (:require
   [re-frame.core :as re-frame]
   [survey.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
