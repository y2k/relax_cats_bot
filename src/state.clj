(defn read [] (fn [w]
                ((:state:read_state w))))

(defn write [value] (fn [w]
                      ((:state:write_state w) value)))

(defn effect_handler [default_value world]
  (let [state_atom (atom default_value)]
    (->
     world
     (assoc :state:read_state
            (fn []
              [(deref state_atom) nil]))
     (assoc :state:write_state
            (fn [value]
              [(reset! state_atom value) nil])))))
