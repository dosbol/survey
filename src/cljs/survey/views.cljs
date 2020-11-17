(ns survey.views
  (:require
   [re-frame.core :as re-frame]
   [cljs.pprint :as pprint]
   [survey.subs :as subs]
   [survey.events :as events]))

(defmulti question (fn [q] (:style q)))

(defmethod question :inline-clickable [{:keys [id text options]}]
  (let [answer @(re-frame/subscribe [::subs/answer id])]
    [:div.padding-10
     text
     (for [option options]
       [:a.inline-clickable.padding-10
        {:key option
         :href "#"
         :class (when (= option answer) "selected")
         :on-click #(re-frame/dispatch [::events/answer id option])}
        option])]))

(defmethod question :dropdown [{:keys [id text options]}]
  [:div.padding-10
   text
   [:select {:value (or @(re-frame/subscribe [::subs/answer id]) "")
             :on-change #(re-frame/dispatch [::events/answer id (.. % -target -value)])}
    [:option {:value ""} "Select option"]
    (for [option options]
      [:option {:key option
                :value option}
       option])]])

(defmethod question :textarea [{:keys [id text]}]
  [:div.flex.padding-10
   text
   [:textarea {:value @(re-frame/subscribe [::subs/answer id])
               :on-change #(re-frame/dispatch [::events/answer id (.. % -target -value)])}]])

(defmethod question :default [{:keys [text]}]
  [:div text])

(defn thanks-panel [customer]
  [:<>
   [:h1 (str customer " Thank you for your feedback")]
   [:div "Data sent to server"
    [:pre (with-out-str (pprint/pprint @(re-frame/subscribe [::subs/sent-data])))]
    [:input {:type "button" :value "Unsubmit"
             :on-click #(re-frame/dispatch [::events/unsubmit])}]]])

(defn questions-panel [customer]
  (let [questions @(re-frame/subscribe [::subs/visible-questions])]
    [:<>
     [:h1 "Hello " customer]
     [:div.flex.column-center.item-start
      (doall
       (for [qwestion questions
             :let [id (:id qwestion)]]
         ^{:key id}
         [question qwestion]))
      [:input.self-end.padding-10.margin-10
       {:type "button"
        :value "Submit"
        :on-click #(re-frame/dispatch [::events/submit])}]]]))

(defn main-panel []
  (let [customer @(re-frame/subscribe [::subs/customer])
        submitted? @(re-frame/subscribe [::subs/submitted?])
        error @(re-frame/subscribe [::subs/error])]
    [:div.flex.column-center.item-center
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

  (every? (fn [{:keys [id values]}] (values (->> id (get answers) :value))) dep)

  ((fn [{:keys [id values]}] (values (get answers id))) (first dep))
  
  :a)
