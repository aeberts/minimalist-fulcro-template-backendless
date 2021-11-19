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
    [com.fulcrologic.semantic-ui.factories :as sui]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul pre]]))

(defsc Tag [this {:tag/keys [:id :desc] :as props}]
  {:query         [:tag/id :tag/desc]
   :ident         :tag/id
   :initial-state {}}
  (div
    (p "Tag: ")
    (pre (pp/write props :stream nil))))

(def ui-tag (comp/factory Tag {:keyfn :tag/id}))

(defsc Task [this {:task/keys [id desc status link tags] :as props}]
  {:query         [:task/id :task/desc :task/status :task/link {:task/tags (comp/get-query Tag)}]
   :ident         :task/id
   :initial-state (fn [params] {:task/tags {}})
   }
  (div))

(def ui-task (comp/factory Task {:keyfn :task/id}))

(defsc Sprint [this {:sprint/keys [id desc tasks] :as props}]
  {:query         [:sprint/id :sprint/desc {:sprint/tasks (comp/get-query Task)}]
   :ident         :sprint/id
   :initial-state (fn [params] {:sprint/tasks (comp/get-initial-state Task)})}
  (div
    (p "Sprint")
    (pre (pp/write props :stream nil))))

(def ui-sprint (comp/factory Sprint {:keyfn :sprint/id}))

;; QUESTION: ask Jakub how to compose :initial-state for Project

(defsc Project [this {:project/keys [id desc tasks] :as props}]
  {:query         [:project/id :project/desc {:project/tasks (comp/get-query Task)}]
   :ident         :project/id
   :initial-state (fn [params] {:project/tasks (comp/get-initial-state Task)})}
  (div
    (p desc)))

(def ui-project (comp/factory Project {:keyfn :project/id}))

(defsc CalendarItem [this {:calendar-item/keys [id desc start-time end-time] :as props}]
  {:query         [:calendar-item/id :calendar-item/desc :calendar-item/start-time :calendar-item/end-time]
   :ident         :calendar-item/id
   :initial-state (fn [params] {})}
  (div
    (p "Calendar item")))

(def ui-calender-item (comp/factory CalendarItem {:keyfn :id}))

(defsc Calendar [this {:keys [:calendar-items] :as props}]
  {:query         [{:calendar-items (comp/get-query CalendarItem)}]
   :ident         (fn [] [:component/id ::Calendar])
   :initial-state (fn [params] {:calendar-items {}})}
  (div :.ui.grid
    (div :.equal.width.row
      (div :.ui.column #js {:style #js {:padding-left "0px" :padding-right "0px"}}
        (sui/ui-button {:content "Week"}))
      (div :.ui.column #js {:style #js {:padding-left "0px" :padding-right "0px"}}
        (sui/ui-button {:content "Day" :floated :right})))
    (div :.row
      (div :.ui.three.wide.column #js {:style #js {:padding-left "0px" :padding-right "0px"}}
        (sui/ui-button {:paddingLeft 0 :size "mini" :content "<" :floated :left}))
      (div :.ui.ten.wide.center.aligned.column
        (p "Nov. 21, 2021"))
      (div :.ui.three.wide.column
        (sui/ui-button {:size "mini" :content ">"})))))

(def ui-calendar (comp/factory Calendar))

(defsc Today [this {:keys [tasks sprints] :as props}]
  {:query         [{:tasks (comp/get-query Task)}
                   {:sprints (comp/get-query Sprint)}]
   :ident         (fn [] [:component/id ::Today])
   :initial-state (fn [params] {:tasks   {}
                                :sprints {}})}
  (div :.ui.grid
    (div :.ui.row
      (div :.ui.four.wide.column.floated.left
        (sui/ui-button {:content "< Archive"}))
      (div :.ui.eight.wide.center.aligned.column
        (h2 "Today"))
      (div :.ui.four.wide.column.floated.right.aligned.right
        (sui/ui-button {:content "This Week >"})))
    (div :.ui.row
      (h3 "Sprints"))
    (div :.ui.row
      (p "Sprints UI"))
    (div :.ui.row.sixteen.wide.column
      (div :.ui.container.center.aligned
        (sui/ui-button {:content "Create Sprint"})))
    (div :.ui.row
      (h3 "Tasks"))
    (div :.ui.row
      (p "Tasks UI"))
    (div :.ui.row.sixteen.wide.column
      (div :.ui.container.center.aligned
        (sui/ui-button {:content "Add a Task"})))))

(def ui-today (comp/factory Today))

(defsc Menu [this {:keys [projects tags] :as props}]
  {:query         [{:projects (comp/get-query Project)}
                   {:tags (comp/get-query Tag)}]
   :ident         (fn [] [:component/id ::Menu])
   :initial-state (fn [params] {:projects {}
                                :tags     {}}
                    )}
  (div
    (h1 "Icon")
    (map ui-project projects)))

(def ui-menu (comp/factory Menu))

(defsc Root [this {:keys [task-filters selected-list today calendar] :as props}]
  {:query [:selected-list
           {:task-filters (comp/get-query Menu)}
           {:today (comp/get-query Today)}
           {:calendar (comp/get-query Calendar)}
           {:tags (comp/get-query Tag)}
           {:projects (comp/get-query Project)}]
   :initial-state
   (fn [_]
     {:selected-list :today
      :task-filters  {:projects [[:project/id 1] [:project/id 2] [:project/id 3] [:project/id 4]]
                      :tags     [[:tag/id 1] [:tag/id 2]]}
      :today         {:tasks   [#:task{:id 1 :desc "Set up OKR Meetings" :status :not-started :link "www.kosmotime.com" :tags []}
                                #:task{:id 2 :desc "Checklist for the PH" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 1 :desc "Urgent"}]}
                                #:task{:id 3 :desc "LinkedIn Strategy" :status :not-started :link "www.linkedin.com" :tags []}]
                      :sprints [#:sprint{:id 1 :desc "App Related Sprint"
                                         :tasks
                                         [#:task{:id 4 :desc "Set up OKR Meetings" :status :not-started :link "www.kosmotime.com" :tags []}
                                          #:task{:id 5 :desc "Checklist for the PH" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 1 :desc "Urgent"}]}
                                          #:task{:id 6 :desc "LinkedIn Strategy" :status :not-started :link "www.linkedin.com" :tags []}]}
                                #:sprint{:id 2 :desc "Planning for the New Website" :tasks []}
                                #:sprint{:id 3 :desc "Product Strategy" :tasks []}]
                      }
      :tags          [#:tag{:id 1 :desc "Urgent"}
                      #:tag{:id 2 :desc "Important"}]
      :projects      [#:project{:id 1 :desc "Today" :tasks [[:task/id 1] [:task/id 2]]}
                      #:project{:id 2 :desc "Uncategorized" :tasks [[:task/id 3] [:task/id 4]]}
                      #:project{:id 3 :desc "Projects" :tasks [[:task/id 4] [:task/id 5]]}
                      #:project{:id 4 :desc "Tags" :tasks [[:task/id 6]]}
                      ]
      :calendar      {:calendar-items [#:calendar-item{:id 1 :desc "Daily Sprint" :start-tiem "9:00am" :end-time "9:15am"}]}
      })}
  (div {:style {:border "1px dashed", :borderColor "blue" :margin "1em", :padding "1em"}}
    (div :.ui.container
      (div :.ui.grid
        (div :.three.wide.computer.column
          (ui-menu task-filters))
        (div :.ten.wide.computer.column
          (cond
            (= selected-list :today) (ui-today today)))
        (div :.three.wide.ui.column
          (ui-calendar calendar))))))

(comment

  (check-props com.example.app/app Root)

  (comp/props
    (comp/class->any com.example.app/app com.example.ui/Project))

  (comp/props
    (comp/class->any com.example.app/app com.example.ui/Menu))

  )

