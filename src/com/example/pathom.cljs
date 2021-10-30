(ns com.example.pathom
  "The Pathom parser that is our (in-browser) backend.
   
   Add your resolvers and 'server-side' mutations here."
  (:require
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc]))

(pc/defresolver index-explorer
  "This resolver is necessary to make it possible to use 'Load index' in Fulcro Inspect - EQL"
  [env _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (-> (get env ::pc/indexes)
     (update ::pc/index-resolvers #(into {} (map (fn [[k v]] [k (dissoc v ::pc/resolve)])) %))
     (update ::pc/index-mutations #(into {} (map (fn [[k v]] [k (dissoc v ::pc/mutate)])) %)))})

;; QUESTION: Any way to reload the data without having to save, reload the page, re-load Pathom index and requery?

(pc/defresolver all-data
  "this returns a fake version of all the todos for debugging purposes
  the data is not normalized
  return the data you want to see in the UI and then go back and clean it up later
  "
  [env _]
  {::pc/input  #{}
   ::pc/output [:all-data]}
  {:all-data
             {:left-sidebar {
                             :today         {
                                             :sprints [
                                                       #:sprint{:id 1 :desc "App Related Sprint"
                                                                :tasks
                                                                    [#:task{:id 1 :desc "Set up OKR Meetings" :status :not-started :link "www.kosmotime.com"}
                                                                     #:task{:id 2 :desc "Checklist for the PH" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 1 :desc "Urgent"}]}
                                                                     #:task{:id 3 :desc "LinkedIn Strategy" :status :not-started :link ""}]}
                                                       #:sprint{:id 2 :desc "Planning for the New Website" :tasks []}
                                                       #:sprint{:id 3 :desc "Product Strategy" :tasks []}]
                                             :tasks
                                                      [#:task{:id 4 :desc "Product Planning" :status :not-started :link "www.kosmotime.com" :tags []}
                                                       #:task{:id 5 :desc "KosmoTime (Desktop Version)" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 2 :desc "Important"}]}
                                                       #:task{:id 6 :desc "Presentation - Product" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 1 :desc "Urgent"}
                                                                                                                                                         #:tag{:id 2 :desc "Important"}]}
                                                       #:task{:id 7 :desc "KosmoTime (Desktop Version)" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 2 :desc "Important"}]}]}
                             :uncategorized {
                                             :tasks
                                             [#:task{:id 8 :desc "Lorem Ipsum" :status :not-started :link "www.kosmotime.com" :tags []}
                                              #:task{:id 9 :desc "KosmoTime (Lorem Ipsum)" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 2 :desc "Important"}]}
                                              #:task{:id 10 :desc "Presentation Mobile App" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 1 :desc "Urgent"}
                                                                                                                                                  #:tag{:id 2 :desc "Important"}]}
                                              #:task{:id 11 :desc "KosmoTime (Dreamcast Version)" :status :not-started :link "www.kosmotime.com" :tags [#:tag{:id 2 :desc "Important"}]}]}
                             :projects      [
                                             #:project{:id 1 :desc "KosmoTimeApp" :tasks [#:task{:id 12 :desc "Foo"}]}
                                             #:project{:id 2 :desc "Private Stuff" :tasks [#:task{:id 13 :desc "Bar"}]}
                                             #:project{:id 3 :desc "KosmoTimeApp" :tasks [#:task{:id 14 :desc "Bing"}]}
                                             ]
                             :tags          [
                                             #:tag{:id 1 :desc "Urgent"}
                                             #:tag{:id 2 :desc "Important"}
                                             #:tag{:id 3 :desc "Priority"}
                                             #:tag{:id 4 :desc "Bug"}
                                             #:tag{:id 5 :desc "Won't Fix"}]}
              :calendar {:current-time "7:45am" :appointments [
                                                               #:appointment{:id 1 :desc "Presentation for N. and M." :start-time "8:15am" :end-time "8:45am"}
                                                               #:appointment{:id 1 :desc "Presentation for N. and M." :start-time "9:00am" :end-time "10:00am"}
                                                               ]}}})


(pc/defresolver i-fail
  [_ _]
  {::pc/input  #{}
   ::pc/output [:i-fail]}
  (throw (ex-info "Fake resolver error" {})))

(pc/defmutation create-random-thing [env {:keys [tmpid] :as params}]
  ;; Fake generating a new server-side entity with
  ;; a server-decided actual ID
  ;; NOTE: To match with the Fulcro-sent mutation, we
  ;; need to explicitly name it to use the same symbol
  {::pc/sym    'com.example.mutations/create-random-thing
   ::pc/params [:tempid]
   ::pc/output [:tempids]}
  (println "SERVER: Simulate creating a new thing with real DB id 123" tmpid)
  {:tempids {tmpid 123}})

(def my-resolvers-and-mutations
  "Add any resolvers you make to this list (and reload to re-create the parser)"
  [index-explorer create-random-thing i-fail all-data])

(defn new-parser
  "Create a new Pathom parser with the necessary settings"
  []
  (p/parallel-parser
    {::p/env     {::p/reader                 [p/map-reader
                                              pc/parallel-reader
                                              pc/open-ident-reader]
                  ::pc/mutation-join-globals [:tempids]}
     ::p/mutate  pc/mutate-async
     ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers-and-mutations})
                  p/error-handler-plugin
                  p/request-cache-plugin
                  (p/post-process-parser-plugin p/elide-not-found)]}))
