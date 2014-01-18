(ns yabi-copy.localfs
  (:use yabi-copy.url)  
  (:import [java.util UUID]))

(defn symlink [src dst]
  ; TODO
  (println "ln -s" (uri-path src) (uri-path dst)))

(defn cp [src dst]
  ; TODO
  (println "cp" (uri-path src) (uri-path dst)))

(defn create-fifo []
  ; TODO
  (str "file:///tmp/yabi_" (UUID/randomUUID)))
