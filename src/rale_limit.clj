(ns _ (:require ["./vendor/effects/effects" :as e]
                ["./state" :as s]
                ["./date" :as date]))

(defn limit [update base_fx]
  (e/then
   (e/batch [(date/get_now)
             (s/read)])
   (fn [[now state]]
    ;;  (eprintln __LOC__
    ;;            update?.message?.from?.id
    ;;            (or (get state (or update?.message?.from?.id update?.callback_query?.from?.id)) 0))
     (if-let [user_id (or update?.message?.from?.id update?.callback_query?.from?.id)
              _ (> (- now (or (get state user_id) 0)) 1500)]
       (e/batch [(s/write (assoc state user_id now))
                 base_fx])
       (e/pure nil)))))
