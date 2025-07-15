(ns _ (:require
       ["../src/command_cat" :as cat]
       ["../src/date" :as date]
       ["../src/main" :as main]
       ["../src/state" :as state]
       ["../src/rale_limit" :as rl]
       ["../src/vendor/effects-promise/index" :as io]
       ["../src/vendor/effects/effects" :as e]
       ["../src/vendor/fetch/index" :as f]
       ["./vendor/edn/main" :as edn]
       ["http" :as http]))

(defn- create_message [text]
  {:update_id 569127348
   :message {:message_id 6707
             :from {:id 241854720
                    :is_bot false
                    :first_name :Igor
                    :username :angmarr
                    :language_code :en
                    :is_premium true}
             :chat {:id 241854720
                    :first_name :Igor
                    :username :angmarr
                    :type :private}
             :date 1752342437
             :text text
             :entities [{:offset 0
                         :length 4
                         :type :bot_command}]}})

;; (let [requests (atom [])
;;       env {:fetch:fetch (fn [x]
;;                           (swap! requests (fn [xs] (conj xs x)))
;;                           ((io/pure {:data {:images {:original {:mp4 "img"}}}}) nil))}]
;;   (.then
;;    (first
;;     ((cat/try_handle_cat_command (create_message "/cat")) env))
;;    (fn []
;;      (eprintln __LOC__ (last (deref requests))))))

;; ====================================================================

;; (defn- make_handler []
;;   (fn [req res]
;;     (.on req :data (fn [chunk]
;;                      (main/handle
;;                       (->> (main/make_env)
;;                            (f/attach_env process.env ["TG_TOKEN" "GIPHY_TOKEN"])
;;                            (f/attach_log))
;;                       (JSON.parse chunk))))
;;     (.on req :end (fn [] (eprintln "END")))
;;     (.writeHead res 200 {"Content-Type" "text/plain"})
;;     (.end res "OK\n")))

;; (let [server (.createServer http (make_handler))]
;;   (.listen server 8080 (fn [] (println "Сервер запущен на порту 8080"))))

;; ====================================================================

(defn call_effect [id]
  (rl/limit
   (create_message "/cat")
   (e/thunk (fn [] (eprintln "Inner Effect called:" id)))))

((e/batch [(call_effect 1)
           (call_effect 2)])
 (->>
  {}
  (date/effect_handler)
  (state/effect_handler {:users {}})))

;; (eprintln
;;  __LOC__
;;  ((date/get_now) (date/effect_handler {})))
