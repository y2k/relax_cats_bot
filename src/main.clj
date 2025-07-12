(ns _ (:require
       ["./vendor/effects-promise/index" :as io]
       ["./vendor/effects/effects" :as e]
       ["./vendor/fetch/index" :as f]
       ["./command_cat" :as cat]))

;; (defn- state [db] (e/call :db db))

;; (defn- handle_rate_limit [data]
;;   (if-let [user_id (or data?.update?.message?.from?.id data?.update?.callback_query?.from?.id)
;;            _ (> (- data.now (or (get data.db user_id) 0)) 1500)]
;;     (e/seq (state (assoc data.db user_id data.now))
;;            (e/dispatch :telegram data.update))
;;     (e/pure nil)))

;; (def GLOBAL_REQUEST_TIMES (atom {}))

(defn make_env []
  (->> {}
       (f/attach_effect_handler)
       (f/attach_decoder)))

(defn handle [env data]
  ((io/first_some [(cat/try_handle_cat_command data)
                   (cat/try_handle_button_click data)])
   env))

(export-default
 {:fetch
  (fn [request env ctx]
    (->
     (.json request)
     (.then (fn [update] (first (handle (->>
                                         (make_env)
                                         (f/attach_env env ["TG_TOKEN" "GIPHY_TOKEN"]))
                                        update))))
     (.catch console.error)
     (.then (fn [] (Response. (str "OK - " (Date.)))))))})
