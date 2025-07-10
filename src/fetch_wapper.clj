
;; (eprintln process.env.TG_TOKEN)
;; (eprintln process.env.GIPHY_TOKEN)

(defn attach_effect_handler [env_atom]
  (let [old_handler (:fetch:fetch (deref env_atom))]
    (swap! env_atom
           (fn [env]
             (assoc env
                    :fetch:fetch
                    (fn [args]
                      (old_handler
                       (assoc args :url
                              (->
                               (:url args)
                               (.replace "~TG_TOKEN~" process.env.TG_TOKEN)
                               (.replace "~GIPHY_TOKEN~" process.env.GIPHY_TOKEN))))))))))
