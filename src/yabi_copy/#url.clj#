(ns yabi-copy.url
  (:require [clojurewerkz.urly.core :as urly]))

(defn uri-protocol [uri]
  (urly/protocol-of (urly/url-like uri)))

(defn uri-host [uri]
  (urly/host-of (urly/url-like uri)))

(defn uri-path [uri]
  (urly/path-of (urly/url-like uri)))

