(ns thrash.htm
  "Convenience functions for working with HTML")

(defn get-body []
  (.-body js/document))