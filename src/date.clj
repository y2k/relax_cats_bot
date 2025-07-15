(defn get_now [] (fn [w]
                   ((:now:now w))))

(defn effect_handler [w]
  (assoc w :now:now (fn [] [(unixtime) nil])))
