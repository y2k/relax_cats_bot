(ns _ (:require ["./vendor/edn/main" :as edn]
                ["../src/vendor/effects/effects" :as e]
                ["../src/vendor/effects-promise/index" :as ep]
                ["../src/vendor/fetch/index" :as f]
                ["../src/command_cat" :as cat]
                ["../src/fetch_wapper" :as fw]
                ["http" :as http]))

;; ((cat/invoke {:update_id 569127348
;;               :message {:message_id 6707
;;                         :from {:id 241854720
;;                                :is_bot false
;;                                :first_name :Igor
;;                                :username :angmarr
;;                                :language_code :en
;;                                :is_premium true}
;;                         :chat {:id 241854720
;;                                :first_name :Igor
;;                                :username :angmarr
;;                                :type :private}
;;                         :date 1752342437
;;                         :text "/cat"
;;                         :entities [{:offset 0
;;                                     :length 4
;;                                     :type :bot_command}]}})
;;  {:fetch:fetch (fn [x]
;;                   ;; (eprintln "FETCH:\n" (edn/to_string_pretty x))
;;                  (eprintln "FETCH:" (JSON.stringify x nil 2))
;;                  [(Promise.resolve {:data {:images {:original {:mp4 "img"}}}}) nil])})

(def env_atom (atom {}))

(f/attach_effect_handler env_atom)
(fw/attach_effect_handler env_atom)

(defn handler [req res]
  (.on req "data" (fn [chunk]
                    ;; (eprintln "DATA:" (edn/to_string (JSON.parse chunk)))
                    ((cat/invoke (JSON.parse chunk)) (deref env_atom))))
  (.on req "end" (fn [] (eprintln "END")))

  (.writeHead res 200 {"Content-Type" "text/plain"})
  (.end res "OK\n"))

(let [server (.createServer http handler)]
  (.listen server 8080 (fn [] (println "Сервер запущен на порту 8080"))))
