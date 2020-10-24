(ns survey.subs
  (:require
   [re-frame.core :as re-frame]
   [clojure.string :as str]))

(re-frame/reg-sub
 ::db
 (fn [db] db))

(re-frame/reg-sub
 ::error
 (fn [db] (:error db)))

(re-frame/reg-sub
 ::customer
 (fn [db]
   (:customer db)))

(re-frame/reg-sub
 ::survey
 (fn [db]
   (:survey db)))

(re-frame/reg-sub
 ::submitted?
 (fn [db]
   (:submitted? db)))

(re-frame/reg-sub
 ::questions
 (fn [db]
   (:questions db)))

(re-frame/reg-sub
 ::answers
 (fn [db]
   (:answers db)))

(re-frame/reg-sub
 ::answer
 :<- [::answers]
 (fn [answers [_ id]]
   (get answers id)))

(defn visible? [questions answers id]
  (let [[{:keys [depends-on]}] (filter #(= (:id %) id) questions)]
    (if-not depends-on
      true
      (and (visible? questions answers (:id depends-on))
           ((:values depends-on) (get answers (:id depends-on)))))))

(re-frame/reg-sub
 ::visible?
 :<- [::questions]
 :<- [::answers]
 (fn [[questions answers] [_ id]]
   (visible? questions answers id)))

(re-frame/reg-sub
 ::sent-data
 :<- [::questions]
 :<- [::answers]
 :<- [::customer]
 :<- [::survey]
 (fn [[questions answers customer survey] _]
   (let [visible-answers (->> (keys answers)
                              (filter (partial visible? questions answers))
                              (select-keys answers))]
     {:customer customer
      :survey (-> survey (str/split #"/") last)
      :answers visible-answers})))
