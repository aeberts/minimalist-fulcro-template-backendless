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
  (div
    (p "Tag: ")
    (pre (pp/write props :stream nil))))

(def ui-tag (comp/factory Tag {:keyfn :tag/id}))

(defsc Task [this {:task/keys [id desc status link tags] :as props}]
  {:query         [:task/id :task/desc :task/status :task/link {:task/tags (comp/get-query Tag)}]
   :ident         :task/id
   :initial-state {}}
  (div
    (p "Task: ")
    (pre (pp/write props :stream nil))))

(def ui-task (comp/factory Task {:keyfn :task/id}))

(defsc Sprint [this {:sprint/keys [id desc tasks] :as props}]
  {:query         [:sprint/id :sprint/desc {:sprint/tasks (comp/get-query Task)}]
   :ident         :sprint/id
   :initial-state (fn [params] {:sprint/tasks (comp/get-initial-state Task)})}
  (div
    (p "Sprint")
    (pre (pp/write props :stream nil))))

(def ui-sprint (comp/factory Sprint {:keyfn :sprint/id}))

(defsc Uncategorized [this {:keys [tasks] :as props}]
  {:query         [{:tasks (comp/get-query Task)}]
   :ident         (fn [] [:component/id ::uncategorized])
   :initial-state {}}
  (div
    (pre (pp/write props :stream nil))))

(def ui-uncategorized (comp/factory Uncategorized))

(defsc Project [this {:project/keys [id desc tasks] :as props}]
  {:query         [:project/id :project/desc {:project/tasks (comp/get-query Task)}]
   :ident         :project/id
   :initial-state (fn [params] {:project/tasks (comp/get-initial-state Task)})}
  (div
    (p "Project")
    (pre (pp/write props :stream nil))
    ))

(def ui-project (comp/factory Project {:keyfn :id}))

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
   :initial-state {}}
  (dom/div
    (p "Calendar Pane")))

(def ui-calendar (comp/factory Calendar))

(defsc Today [this {:keys [tasks sprints] :as props}]
  {:query         [{:tasks (comp/get-query Task)}
                   {:sprints (comp/get-query Sprint)}]
   :ident         (fn [] [:component/id ::Today])
   :initial-state (fn [params] {:tasks   {}
                                :sprints {}})}
  (div
    (p "Today Pane")
    ;;(pre (pp/write (str props) :stream nil))
    ))

(def ui-today (comp/factory Today))

(defsc Menu [this {:keys [projects tags] :as props}]
  {:query         [{:projects (comp/get-query Project)}
                   {:tags (comp/get-query Tag)}]
   :ident         (fn [] [:component/id ::Menu])
   :initial-state (fn [params] {:projects {}
                                :tags     {}})}
  (div
    (p "Menu Pane")
    ;(pre (pp/write (str props) :stream nil))
    ))

(def ui-menu (comp/factory Menu))

(defsc Root [this {:keys [task-filters selected-list today calendar] :as props}]
  {:query [:selected-list
           {:task-filters (comp/get-query Menu)}
           {:today (comp/get-query Today)}
           {:calendar (comp/get-query Calendar)}]
   :initial-state
   (fn [_]
     {:selected-list :today
      :task-filters  {:projects [[:project/id 1] [:project/id 2]]
                      :tags     [[:tag/id 1] [:tag/id 2]]}
      :today         {:tasks   [#:task{:id 1 :desc "Set up OKR Meetings" :status :not-started :link "www.kosmotime.com"}
                                #:task{:id 2 :desc "Checklist for the PH" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 1 :desc "Urgent"}]}
                                #:task{:id 3 :desc "LinkedIn Strategy" :status :not-started :link "www.linkedin.com"}]
                      :sprints [#:sprint{:id 1 :desc "App Related Sprint"
                                         :tasks
                                         [#:task{:id 4 :desc "Set up OKR Meetings" :status :not-started :link "www.kosmotime.com"}
                                          #:task{:id 5 :desc "Checklist for the PH" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 1 :desc "Urgent"}]}
                                          #:task{:id 6 :desc "LinkedIn Strategy" :status :not-started :link "www.linkedin.com"}]}
                                #:sprint{:id 2 :desc "Planning for the New Website" :tasks []}
                                #:sprint{:id 3 :desc "Product Strategy" :tasks []}]
                      }
      :tags          [#:tag{:id 1 :desc "Urgent"}
                      #:tag{:id 2 :desc "Important"}]
      :projects      [#:project{:id 1 :desc "Home"}
                      #:project{:id 2 :desc "Work"}]
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
        (div :.three.wide.computer.column
          (ui-calendar calendar))))))

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

  (check-props com.example.app/app Root))

