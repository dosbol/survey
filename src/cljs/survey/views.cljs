(ns survey.views
  (:require
   [re-frame.core :as re-frame]
   [cljs.pprint :as pprint]
   [survey.subs :as subs]
   [survey.events :as events]))

(defmulti question (fn [q] (:style q)))

(defmethod question :inline-clickable
  [q]
  (let [visible? @(re-frame/subscribe [::subs/visible? (:id q)])
        answer @(re-frame/subscribe [::subs/answer (:id q)])]
    (when visible?
      [:div {:style {:padding 10}}
       (:text q)
       (doall (for [o (:options q)]
                ^{:key o}
                [:a {:href "#"
                     :style {:padding 10
                             :border "solid 1px"
                             :border-radius 10
                             :margin 1
                             :text-decoration :none
                             :background-color (when (= o answer) :darkgrey)}
                     :on-click #(re-frame/dispatch [::events/answer (:id q) o])} o]))])))

(defmethod question :dropdown
  [q]
  (when @(re-frame/subscribe [::subs/visible? (:id q)])
    [:div {:style {:padding 10}}
     (:text q)
     [:select {:on-change #(re-frame/dispatch [::events/answer (:id q) (.-value (.-target %))])
               :value (or @(re-frame/subscribe [::subs/answer (:id q)]) "")}
      [:option {:value ""} "Select option"]
      (doall (for [o (:options q)]
               ^{:key o}
               [:option {:value o} o]))]]))

(defmethod question :textarea
  [q]
  (when @(re-frame/subscribe [::subs/visible? (:id q)])
    [:div  {:style {:padding 10
                    :display :flex
                    :justify-content :center}}
     (:text q)
     [:textarea {:on-change #(re-frame/dispatch [::events/answer (:id q) (.. % -target -value)])}]]))

(defmethod question :default
  [q]
  (when @(re-frame/subscribe [::subs/visible? (:id q)])
    [:div (:text q)]))

(defn thanks-panel [customer]
  [:<>
   [:h1 (str customer " Thank you for your feedback")]
   [:div "Data sent to server"
    [:pre (with-out-str (pprint/pprint @(re-frame/subscribe [::subs/sent-data])))]
    [:input {:type "button" :value "Unsubmit"
             :on-click #(re-frame/dispatch [::events/unsubmit])}]]])

(defn questions-panel [customer]
  (let [questions @(re-frame/subscribe [::subs/questions])]
    [:<>
     [:h1 "Hello " customer]
     [:div {:style {:display :flex
                    :align-items :flex-start
                    :flex-direction :column
                    :justify-content :center}}
      (doall (for [q questions]
               ^{:key (:id q)}
               [question q]))
      [:input {:type "button" :value "Submit" :style {:padding 10
                                                      :align-self :flex-end}
               :on-click #(re-frame/dispatch [::events/submit])}]]]))

(defn main-panel []
  (let [customer @(re-frame/subscribe [::subs/customer])
        submitted? @(re-frame/subscribe [::subs/submitted?])
        error @(re-frame/subscribe [::subs/error])]
    [:div {:style {:display :flex
                   :align-items :center
                   :flex-direction :column
                   :justify-content :center}}
     (if submitted?
       [thanks-panel customer]
       [questions-panel customer])
     (when error
       [:div "Something went wrong: " error])]))

(comment

  (def questions @(re-frame/subscribe [::subs/questions]))

  (into [:<>] (mapv question questions))

  @(re-frame/subscribe [::subs/visible? (:depends-on (get questions 1))])

  (def dep #{{:id :experience-level, :values #{"Very Bad" "Bad"}}})

  (def answers @(re-frame/subscribe [::subs/answers]))

  (every? (fn [{:keys [id values]}] (prn id ";" values) (values (->> id (get answers) :value))) dep)

  ((fn [{:keys [id values]}] (prn (->> id (get answers))) (values (get answers id))) (first dep))

  (#{1 2} 2)

  :a)
