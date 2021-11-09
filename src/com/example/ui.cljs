(ns com.example.ui
  (:require
    [cljs.pprint :as pp]
    [com.example.mutations :as mut]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul pre]]))

(defsc Tag [this {:tag/keys [:id :desc] :as props}]
  {:query         [:tag/id :tag/desc]
   :ident         :tag/id
   :initial-state {}}
  (dom/div props))

(def ui-tag (comp/factory Tag {:keyfn :tag/id}))

(defsc Task [this {:task/keys [id desc status link tags] :as props}]
  {:query         [:task/id :task/desc :task/status :task/link {:task/tags (comp/get-query Tag)}]
   :ident         :task/id
   :initial-state {}}
  (div
    (p "Task Props: " props)))

(def ui-task (comp/factory Task {:keyfn :task/id}))

(defsc Sprint [this {:sprint/keys [id desc tasks] :as props}]
  {:query         [:sprint/id :sprint/desc {:sprint/tasks (comp/get-query Task)}]
   :ident         :sprint/id
   :initial-state (fn [params] {:sprint/tasks (comp/get-initial-state Task)})}
  (div
    (p props)))

(def ui-sprint (comp/factory Sprint {:keyfn :sprint/id}))

(defsc TodayPane [this {:keys [sprints tasks] :as props}]
  ;; Another approach would be to have a component called SprintList that contains individual sprint items
  ;; this would also apply to tasks i.e. TaskList component which would contain Task items.
  {:query         [{[:sprints '_] (comp/get-query Sprint)}
                   {[:tasks '_] (comp/get-query Task)}]
   :ident         (fn [] [:component/id ::today-pane])
   :initial-state (fn [params] {:sprints (comp/get-initial-state Sprint)
                                :tasks   (comp/get-initial-state Task)})
   }
  (div
    (p "Today Panel props: " props)))

(def ui-today-pane (comp/factory TodayPane))

(defsc Uncategorized [this {:keys [tasks] :as props}]
  {:query         [{:tasks (comp/get-query Task)}]
   :ident         (fn [] [:component/id ::uncategorized])
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

(defsc Menu [this {:keys [today uncategorized projects tags] :as props}]
  {:query         [
                   {:today (comp/get-query TodayPane)}
                   {:uncategorized (comp/get-query Uncategorized)}
                   {:projects (comp/get-query Project)}
                   {:tags (comp/get-query Tag)}
                   ]
   :ident         (fn [] [:component/id :Menu])
   :initial-state (fn [params] {:today         (comp/get-initial-state TodayPane)
                                :uncategorized (comp/get-initial-state Uncategorized)})
   }
  (div
    ;(pre "Menu props: \n " (pp/write props :stream nil))
    ))

(def ui-menu (comp/factory Menu))

;;(defsc CalendarPane [this {:keys [:id    ] :as props}]
;;  {:query [:id    ]
;;   :ident :id
;;   :initial-state {}}
;;  (dom/div ))
;;
;;(def ui-calendar-pane (comp/factory CalendarPane))

(defsc Root [this {:keys [task-filters selected-list sprints tasks today] :as props}]
  {:query [:selected-list
           {:task-filters (comp/get-query Menu)}
           {:tasks (comp/get-query Task)}
           {:sprints (comp/get-query Sprint)}
           {:today (comp/get-query TodayPane)}]
   :initial-state
          (fn [_]
            {:selected-list :today
             :task-filters  {:projects [#:project{:id 1 :desc "Home"}
                                        #:project{:id 2 :desc "Work"}]
                             :tags     [#:tag{:id 1 :desc "Urgent"}
                                        #:tag{:id 2 :desc "Important"}]}
             :today         {:tasks   [#:task{:id 1 :desc "Set up OKR Meetings" :status :not-started :link "www.kosmotime.com"}
                                       #:task{:id 2 :desc "Checklist for the PH" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 1 :desc "Urgent"}]}
                                       #:task{:id 3 :desc "LinkedIn Strategy" :status :not-started :link "www.linkedin.com"}]
                             :sprints [#:sprint{:id 1 :desc "App Related Sprint"
                                                :tasks
                                                    [#:task{:id 1 :desc "Set up OKR Meetings" :status :not-started :link "www.kosmotime.com"}
                                                     #:task{:id 2 :desc "Checklist for the PH" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 1 :desc "Urgent"}]}
                                                     #:task{:id 3 :desc "LinkedIn Strategy" :status :not-started :link "www.linkedin.com"}]}
                                       #:sprint{:id 2 :desc "Planning for the New Website" :tasks []}
                                       #:sprint{:id 3 :desc "Product Strategy" :tasks []}]}

             }
            )
   }
  (div {:style {:border "1px dashed", :borderColor "red" :margin "1em", :padding "1em"}}
    (p "Left Menu Pane:")
    (div :.ui-container
      (div :.ui-grid
        (div :.sixteen.wide.mobile.four.wide.computer.column
          (ui-menu task-filters))
        (div :.sixteen.wide.mobile.twelve.wide.computer.column
          (cond
            (= selected-list :today) (ui-today-pane today)))

        ))))

(comment

  (cljs.pprint/write {:a 1 :b 2} :stream nil)

  )

(defn check-props
  "Check the state for particular component
  app is usually at com.example.app/app or similar
  From: https://github.com/holyjak/blog.jakubholy.net/blob/master/content/asc/posts/2020/troubleshooting-fulcro.asc"
  ([app]
   (check-props app Root))
  ([app comp]
   (let [state (app/current-state app)]
     (com.fulcrologic.fulcro.algorithms.denormalize/db->tree
       (comp/get-query comp)                                ; or any component
       ;; Starting entity, state itself for Root
       ;; otherwise something like (get-in state-map [:thing/id 1]):
       state
       state))))

(comment

  (check-props com.example.app/app Root)

  )