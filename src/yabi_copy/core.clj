(ns yabi-copy.core
  (:use yabi-copy.url)
  (:require [yabi-copy [ssh :as ssh] [s3 :as s3] [localfs :as localfs]]))


(defn- same-place? [uri other-uri]
  (and (= (uri-protocol uri) (uri-protocol other-uri))
       (= (uri-host uri) (uri-host other-uri))))

(defn- cp-dispatch [src dst &{ :keys [symlink-allowed local-copy-allowed]
                                     :or {symlink-allowed true local-copy-allowed true}}]
  (cond
   (and (same-place? src dst) symlink-allowed) [(uri-protocol src) :symlink]
   (and (same-place? src dst) local-copy-allowed) [(uri-protocol src) :local-copy]
   :default [(uri-protocol src) (uri-protocol dst)]))

(ns-unmap *ns* 'cp)
(defmulti cp cp-dispatch)

(defmethod cp ["sftp" :symlink] [src dst & _]
  (ssh/symlink src dst))

(defmethod cp ["sftp" :local-copy] [src dst & _]
  (ssh/local-copy src dst))

(defmethod cp ["sftp" "file"] [src dst & _]
  (ssh/get src dst))

(defmethod cp ["file" "sftp"] [src dst & _]
  (ssh/put src dst))


(defmethod cp ["s3" "file"] [src dst & _]
  (s3/get src dst))

(defmethod cp ["file" "s3"] [src dst & _]
  (s3/put src dst))


(defmethod cp ["file" :symlink] [src dst & _]
  (localfs/symlink src dst))

(defmethod cp ["file" :local-copy] [src dst & _]
  (localfs/cp src dst))

(defmethod cp ["file" "file"] [src dst & _]
  (if (same-place? src dst)
    (throw (IllegalArgumentException. "Local copy has to be allowed for file to file copy"))
    (throw (IllegalArgumentException. "Can copy file to file only on the same host"))))


(defmethod cp :default [src dst & _]
  (let [fifo (localfs/create-fifo)]
    (comment ; For demo purposes we don't do the cp in a separate
             ; thread (messes up the output)
      (future (cp src fifo)))
    (cp src fifo)
    (cp fifo dst)))

                                        ; Usage

(comment
  
  (cp "sftp://epic.ivec.org/home/tszabo/a.txt" "file:///home/sztamas/")
      ; SFTP get sftp://epic.ivec.org/home/tszabo/a.txt to file:///home/sztamas/
  (cp "file:///home/sztamas/a.txt" "sftp://epic.ivec.org/home/tszabo/")
      ; SFTP put file:///home/sztamas/a.txt to sftp://epic.ivec.org/home/tszabo/
  (cp "sftp://epic.ivec.org/home/tszabo/a.txt" "sftp://ccg.murdoch.edu.au/home/tszabo/")
      ; SFTP get sftp://epic.ivec.org/home/tszabo/a.txt to file:///tmp/yabi_18307199-8d15-4550-9ea2-94c4482e0557
      ; SFTP put file:///tmp/yabi_18307199-8d15-4550-9ea2-94c4482e0557 to sftp://ccg.murdoch.edu.au/home/tszabo/
  (cp "sftp://epic.ivec.org/home/tszabo/a.txt" "sftp://epic.ivec.org/home/tszabo/a_dir/")
      ; SSH login then ln -s sftp://epic.ivec.org/home/tszabo/a_dir/ to sftp://epic.ivec.org/home/tszabo/a.txt
  (cp "sftp://epic.ivec.org/home/tszabo/a.txt" "sftp://epic.ivec.org/home/tszabo/a_dir/" :symlink-allowed false)
      ; SSH login then cp sftp://epic.ivec.org/home/tszabo/a.txt to sftp://epic.ivec.org/home/tszabo/a_dir/
  (cp "sftp://epic.ivec.org/home/tszabo/a.txt" "sftp://epic.ivec.org/home/tszabo/a_dir/" :symlink-allowed false :local-copy-allowed false)
      ; SFTP get sftp://epic.ivec.org/home/tszabo/a.txt to file:///tmp/yabi_18307199-8d15-4550-9ea2-94c4482e0557
      ; SFTP put file:///tmp/yabi_18307199-8d15-4550-9ea2-94c4482e0557 to sftp://ccg.murdoch.edu.au/home/tszabo/

  (cp "sftp://epic.ivec.org/home/tszabo/a.txt" "s3://our-bucket.amazonaws.com/home/tszabo/")
      ; SFTP get sftp://epic.ivec.org/home/tszabo/a.txt to file:///tmp/yabi_dcf7c09c-ff28-4052-a10e-4895fb58f60b
      ; S3 put file:///tmp/yabi_dcf7c09c-ff28-4052-a10e-4895fb58f60b to s3://our-bucket.amazonaws.com/home/tszabo/

  ; S3 no symlink or local copy capabilities
  (cp "s3://our-bucket.amazonaws.com/home/tszabo/a.txt" "s3://our-bucket.amazonaws.com/home/tszabo/a_dir/")
      ; S3 get s3://our-bucket.amazonaws.com/home/tszabo/a.txt to file:///tmp/yabi_f7205949-9405-4c2e-a825-4fe9599932ee
      ; S3 put file:///tmp/yabi_f7205949-9405-4c2e-a825-4fe9599932ee to s3://our-bucket.amazonaws.com/home/tszabo/a_dir/

  (cp "file:///home/sztamas/a.txt" "file:///tmp/")
      ; ln -s /home/sztamas/a.txt /tmp/
  (cp "file:///home/sztamas/a.txt" "file:///tmp/" :symlink-allowed false)
      ; cp /home/sztamas/a.txt /tmp/
  (cp "file:///home/sztamas/a.txt" "file:///tmp/" :symlink-allowed false :local-copy-allowed false)
      ; IllegalArgumentException Local copy has to be allowed for file to file copy  yabi-copy.core/eval1873/fn--1874 (core.clj:47)
  (cp "file:///home/sztamas/a.txt" "file://epic.ivec.org/tmp/")
      ; IllegalArgumentException Can copy file to file only on the same host  yabi-copy.core/eval1873/fn--1874 (core.clj:47)
  )
