(ns yabi-copy.ssh)

(defn get [src dst]
  ; TODO
  (println "SFTP get" src "to" dst))

(defn put [src dst]
  ; TODO
  (println "SFTP put" src "to" dst))

(defn symlink [src dst]
  ; TODO
  (println "SSH login then ln -s" dst "to" src))

(defn local-copy [src dst]
  ; TODO
  (println "SSH login then cp" src "to" dst))


