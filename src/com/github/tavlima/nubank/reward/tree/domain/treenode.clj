(ns com.github.tavlima.nubank.reward.tree.domain.treenode)

(defprotocol TreeNode
  (match? [node value])
  (uid [node])
  (fields [node ks]))
