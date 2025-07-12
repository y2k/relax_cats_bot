(ns _ (:require
       ["./vendor/effects-promise/index" :as io]))

(def- state (atom {}))

(defn invoke [data base_fx]
  (if-let [user_id (or data?.update?.message?.from?.id data?.update?.callback_query?.from?.id)
           _ (> (- data.now (or (get data.db user_id) 0)) 1500)]
    (io/batch [(state (assoc data.db user_id data.now))
               base_fx])
    (io/pure nil)))
