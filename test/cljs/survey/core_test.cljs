(ns survey.core-test
  (:require
   [cljs.test :refer-macros [deftest is]]
   [day8.re-frame.test :as rf-test]
   [re-frame.core :as rf]
   [survey.events :as events]
   [survey.subs :as subs]
   [survey.coeffects :as cofx]))

(def uri "https://gist.githubusercontent.com/dosbol/dd7b279017f58539fe215b464c6bc731/raw/69df17a39a8a16894cf7f99ad49af9d5b3781415/survey-questions.edn")

(def qs [{:id :experience-level
          :text "How was your experience with us today?"
          :options ["Very good" "Good" "Neutral" "Bad" "Very Bad"]
          :style :inline-clickable}
         {:id :dissatisfaction-reason
          :text "What is the reason for your dissatisfaction?"
          :options ["Agent was rude" "Waiting times are long" "No solution for my problem" "Other"]
          :style :dropdown
          :depends-on {:id :experience-level :values #{"Bad" "Very Bad"}}}
         {:id :dissatisfaction-other-reason
          :text "Please explain the reason for your dissatisfaction"
          :style :textarea
          :depends-on {:id :dissatisfaction-reason :values #{"Other"}}}])

(defn fixtures []
  (rf/reg-cofx
   ::cofx/query-params
   (fn [cofx _]
     (assoc cofx :query-params {:customer "Adidas"
                                :survey uri}))))

(deftest test-async
  (rf-test/run-test-async
    (fixtures)
    (rf/dispatch-sync [::events/initialize])
   
   (rf-test/wait-for [::events/set-questions]

    (let [customer  (rf/subscribe [::subs/customer])
          survey    (rf/subscribe [::subs/survey])
          questions (rf/subscribe [::subs/questions])
          answers   (rf/subscribe [::subs/answers])]
      
      (is (= "Adidas" @customer))
      (is (= uri @survey))
      (is (= qs @questions))
      (is (nil? @answers))))))


(deftest test-sync
  (rf-test/run-test-sync
   (fixtures)
   (rf/dispatch [::events/set-questions qs])

   (let [questions       (rf/subscribe [::subs/questions])
         answers         (rf/subscribe [::subs/answers])
         first-visible?  (rf/subscribe [::subs/visible? :experience-level])
         second-visible? (rf/subscribe [::subs/visible? :dissatisfaction-reason])
         third-visible?  (rf/subscribe [::subs/visible? :dissatisfaction-other-reason])
         sent-data     (rf/subscribe [::subs/sent-data])]

     (is (= qs @questions))
     (is (nil? @answers))
     (is @first-visible?)
     (is (not @second-visible?))
     (is (not @third-visible?))

     (rf/dispatch [::events/answer :experience-level "Good"])
     (is (= "Good"
            (-> @answers :experience-level)))
     (is @first-visible?)
     (is (not @second-visible?))
     (is (not @third-visible?))

     (rf/dispatch [::events/answer :experience-level "Bad"])
     (is (= "Bad"
            (-> @answers :experience-level)))
     (is @first-visible?)
     (is @second-visible?)
     (is (not @third-visible?))

     (rf/dispatch [::events/answer :dissatisfaction-reason "No solution for my problem"])
     (is (= "No solution for my problem"
            (-> @answers :dissatisfaction-reason)))
     (is @first-visible?)
     (is @second-visible?)
     (is (not @third-visible?))

     (rf/dispatch [::events/answer :dissatisfaction-reason "Other"])
     (is (= "Other"
            (-> @answers :dissatisfaction-reason)))
     (is @first-visible?)
     (is @second-visible?)
     (is @third-visible?)

     (rf/dispatch [::events/answer :dissatisfaction-other-reason "Agent was not responsive"])
     (is (= "Agent was not responsive"
            (-> @answers :dissatisfaction-other-reason)))
     (is @first-visible?)
     (is @second-visible?)
     (is @third-visible?)

     (is (= {:experience-level "Bad"
             :dissatisfaction-reason "Other"
             :dissatisfaction-other-reason "Agent was not responsive"}
            (:answers @sent-data))))))
