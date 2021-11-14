(ns com.example.client
  (:require
   [com.example.app :refer [app]]
   [com.example.ui :as ui]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.data-fetch :as df]
   ))

(defn ^:export init
  "Called by shadow-cljs upon initialization, see shadow-cljs.edn"
  []
  (println "Initializing the app...")
  (app/set-root! app ui/Root {:initialize-state? true})
  (dr/initialize! app) ; make ready, if you want to use dynamic routing...

  ;; Not how it's supposed to be done
  ;; load! will create a join between the resolver attribute
  ;; (in this case :left-sidebar) and the query of the component given (in this case LeftSidebar)
  ;;
  ;;(do
  ;;  ;;(df/load! app :all-data ui/Root)
  ;;  ;;(df/load! app :all-tasks ui/Task)
  ;;  ;;(df/load! app :all-tags ui/Tag)
  ;;  ;;(df/load! app :all-sprints ui/Sprint)
  ;;  ;;(df/load! app :all-projects ui/Project)
  ;;  ;(df/load! app :left-sidebar ui/Menu)
  ;;  (df/load! app :today ui/Today)
  ;;  )

  (app/mount! app
              (app/root-class app)
              "app"
              {:initialize-state? false})
  )

(defn ^:export refresh 
  "Called by shadow-cljs upon hot code reload, see shadow-cljs.edn"
  []
  (println "Refreshing after a hot code reload...")
  (comp/refresh-dynamic-queries! app)
  (app/mount! app (app/root-class app) "app")
  )

