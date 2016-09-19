(ns com.github.tavlima.nubank.reward.tree.domain.zippable)

(defprotocol Zippable
  (children [node])
  (make [node children])
  (zipper [node]))
