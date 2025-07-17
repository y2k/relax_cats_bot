(ns _ (:require ["./vendor/effects/effects" :as e]
                ["./vendor/effects-tools/state" :as s]
                ["./vendor/effects-tools/date" :as date]))

(defn limit [update base_fx]
  (e/then
   (e/batch [(date/get_now)
             (s/read)])
   (fn [[now state]]
    ;;  (eprintln __LOC__ now state)
     (if-let [user_id (or update?.message?.from?.id update?.callback_query?.from?.id)
              _ (> (- now (or (get state user_id) 0)) 1500)]
       (e/batch [(s/write (assoc state user_id now))
                 base_fx])
       (e/pure nil)))))
