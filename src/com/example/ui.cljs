(ns com.example.ui
  (:require 
    [com.example.mutations :as mut]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.fulcro.data-fetch :as df]    
    [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]))

(defsc Tag [this {:tag/keys [:id :desc] :as props}]
  {:query [:tag/id :tag/desc]
   :ident :tag/id}
  (dom/div props))

(def ui-tag (comp/factory Tag {:keyfn :tag/id}))

(defsc Task [this {:task/keys [id desc status link tags] :as props}]
  {:query [:task/id :task/desc :task/status :task/link {:task/tags (comp/get-query Tag)}]
   :ident :task/id}
  (p "Task Props: " props))

(def ui-task (comp/factory Task {:keyfn :task/id}))

(defsc Sprint [this {:sprint/keys [id desc tasks ] :as props}]
  {:query         [:sprint/id :sprint/desc {:sprint/tasks (comp/get-query Task)}]
   :ident         :sprint/id
   :initial-state (fn [params] {:sprint/tasks [(comp/get-initial-state Task)]})
   }
  (div
    (p props)))

(def ui-sprint (comp/factory Sprint {:keyfn :sprint/id}))

(defsc TodayPane [this {:keys [sprints tasks] :as props}]
  ;; Another approach would be to have a component called SprintList that contains individual sprint items
  ;; this would also apply to tasks i.e. TaskList component which would contain Task items.
  {:query         [{:sprints (comp/get-query Sprint)}
                   {:tasks (comp/get-query Task)}]
   :ident         (fn [] [:component/id ::today-pane])
   :initial-state (fn [params] {:sprints (comp/get-initial-state Sprint)
                                :tasks (comp/get-initial-state Task)})}
  (div
    (p "Today Panel props: " props)))

(def ui-today-pane (comp/factory TodayPane))

(defsc Uncategorized [this {:keys [tasks] :as props}]
  {:query [{:tasks (comp/get-query Task)}]
   :ident (fn [] [:component/id ::uncategorized])
   :initial-state {}}
  (div
    (p props)))

(def ui-uncategorized (comp/factory Uncategorized))

(defsc Project [this {:project/keys [id desc tasks] :as props}]
  {:query         [:project/id :project/desc {:project/tasks (comp/get-query Task)}]
   :ident         :project/id
   :initial-state (fn [params] {:project/tasks (comp/get-initial-state Task)})}
  (div
    (p props)))

(def ui-project (comp/factory Project {:keyfn :id}))

(defsc LeftSidebar [this {:keys [today uncategorized projects tags] :as props}]
  {:query [{:today (comp/get-query TodayPane)}
           {:uncategorized (comp/get-query Uncategorized)}
           {:projects (comp/get-query Project)}
           {:tags (comp/get-query Tag)}]
   :initial-state (fn [params] {:today (comp/get-initial-state TodayPane)
                                :uncategorized (comp/get-initial-state Uncategorized)
                                :projects (comp/get-initial-state Project)
                                :tags (comp/get-initial-state Tag)})
   :ident (fn [] [:component/id ::left-sidebar])}
  (div
    (p props)
    (ui-today-pane today)))

(def ui-left-sidebar (comp/factory LeftSidebar))

;;(defsc CalendarPane [this {:keys [:id    ] :as props}]
;;  {:query [:id    ]
;;   :ident :id
;;   :initial-state {}}
;;  (dom/div ))
;;
;;(def ui-calendar-pane (comp/factory CalendarPane))

(defsc Root [this {:keys [left-sidebar] :as props}]
  {:query         [{:left-sidebar (comp/get-query LeftSidebar)}
                   [df/marker-table :load-progress] :new-thing]
   :initial-state (fn [params] {:left-sidebar (comp/get-initial-state LeftSidebar)})}
  (div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
    (p "Hello from the ui/Root component!")
    (ui-left-sidebar left-sidebar)
    ;;(div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
    ;;  (p "Invoke a load! that fails and display the error:")
    ;;  (when-let [m (get props [df/marker-table :load-progress])]
    ;;    (dom/p "Progress marker: " (str m)))
    ;;  (button {:onClick #(df/load! this :i-fail (rc/nc '[*]) {:marker :load-progress})} "I fail!"))
    ;;(div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
    ;;  (p "Simulate creating a new thing with server-assigned ID, leveraging Fulcro's tempid support:")
    ;;  (button {:onClick #(let [tmpid (tempid/tempid)]
    ;;                       (comp/transact! this [(mut/create-random-thing {:tmpid tmpid})]))}
    ;;    "I create!")
    ;;  (when-let [things (:new-thing props)]
    ;;    (p (str "Created a thing with the ID: " (first (keys things))))))

    ))