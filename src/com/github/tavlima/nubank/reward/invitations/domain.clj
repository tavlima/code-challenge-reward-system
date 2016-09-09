(ns com.github.tavlima.nubank.reward.invitations.domain
  (require [clojure.zip :as z]))


; User
(defn create-user [id]
  {:id id :score 0 :invited [] :verified false})

(defn add-score
  [user score-inc]
  (merge user {:score (+ (:score user) score-inc)}))

(defn verified? [user]
  (:verified user))


; Tree
(defn create-tree
  ([] (create-tree nil #{}))
  ([root users] {:root root :users users}))

(defn create-tree-with-user [userId]
  (create-tree (create-user userId) (hash-set userId)))

(defn add-first-user
  ([userId]
   (create-tree {:root nil :users #{}} userId))
  ([tree userId]
   (if (and (nil? (:root tree))
            (empty? (:users tree)))
     tree
     (create-tree (create-user userId) (hash-set userId)))
    ))

(defn has-user?
  [tree userId]
  (contains? (:users tree) userId))


; Zipper
(defn branch?
  [_]
  true)

(defn children
  [loc]
  (:invited loc))

(defn make-node
  [node children]
  (merge node {:invited children}))

(defn zipper
  [node]
  (z/zipper branch? children make-node node))

(defn find-first [zipper matcher]
  (loop [loc zipper]
    (if (z/end? loc)
      (z/root loc)
      (if (matcher (z/node loc))
        loc
        (recur (z/next loc))))))

(defn- id-matcher
  [expected actual]
  (= expected (:id actual)))

(defn find-first-by-id
  [zipper id]
  (find-first zipper (partial id-matcher id)))

(defn- simplify-node [ks node]
  (select-keys node ks))

(defn nodes
  ([zipper]
   (nodes zipper [:id :score :verified]))
  ([zipper ks]
   (->> (iterate z/next zipper)
        (take-while (complement z/end?))
        (map z/node)
        (filter (complement nil?))
        (map (partial simplify-node ks)))))
