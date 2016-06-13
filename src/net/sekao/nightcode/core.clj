(ns net.sekao.nightcode.core
  (:require [clojure.java.io :as io]
            [net.sekao.nightcode.boot]
            [net.sekao.nightcode.controller :as c]
            [net.sekao.nightcode.editors :as e]
            [net.sekao.nightcode.projects :as p]
            [net.sekao.nightcode.shortcuts :as shortcuts]
            [net.sekao.nightcode.state :refer [pref-state runtime-state]]
            [clojure.spec :as s])
  (:import [javafx.application Application]
           [javafx.fxml FXMLLoader]
           [javafx.stage Stage StageBuilder]
           [javafx.scene Scene])
  (:gen-class :extends javafx.application.Application))

(def actions {:start c/show-start-menu!
              :import-project c/import!
              :rename c/rename!
              :remove c/remove!
              :up c/up!
              :save c/save!
              ;:undo
              ;:redo
              :instarepl c/toggle-instarepl!
              :find c/focus-on-find!
              :replace c/focus-on-replace!
              :close c/close!})

(defn -start [^net.sekao.nightcode.core app ^Stage stage]
  (let [root (FXMLLoader/load (io/resource "main.fxml"))
        scene (Scene. root 1242 768)
        project-tree (.lookup scene "#project_tree")
        content (.lookup scene "#content")]
    (doto stage
      (.setTitle "Nightcode 2.0.0-SNAPSHOT")
      (.setScene scene)
      (.show))
    (swap! runtime-state assoc :web-port (e/start-web-server!))
    (-> content .getChildren .clear)
    (p/update-project-tree! pref-state project-tree)
    (p/update-project-buttons! @pref-state scene)
    (p/set-selection-listener! pref-state runtime-state stage project-tree content)
    (p/set-focused-listener! pref-state stage project-tree)
    (p/set-project-key-listener! stage)
    (shortcuts/add-tooltips! scene [:project-tree :start :import-project :rename :remove])
    (shortcuts/set-shortcut-listeners! stage actions)))

(defn -main [& args]
  (Application/launch net.sekao.nightcode.core (into-array String args)))

(defn dev-main [& args]
  (s/instrument-all)
  (apply -main args))
