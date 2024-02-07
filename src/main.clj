(require [vendor.effects :as e])

(defn- eff_fetch    [url props] (e/call :fetch    [url props]))
(defn- eff_dispatch [key data]  (e/call :dispatch [key data]))

(def RELOAD_LIMIT 3)

(defn- try_handle_button_click_image [chat_id message_id count tag user_id img_json]
  (eff_fetch "https://api.telegram.org/bot~TG_TOKEN~/editMessageMedia"
             {:method "POST"
              :body (JSON/stringify
                     {:media {:type "video" :media img_json.data.images.original.mp4}
                      :chat_id chat_id
                      :message_id message_id
                      :reply_markup {:inline_keyboard
                                     [[{:text (if (= RELOAD_LIMIT count) "Delete" (+ "Next [" (- RELOAD_LIMIT count) "]"))
                                        :callback_data (.stringify JSON {:c (+ count 1) :u user_id :t tag})}
                                       {:text "Done" :callback_data (.stringify JSON {:c -1 :u user_id :t tag})}]]}})
              :headers {"content-type" "application/json"}}))

(defn- try_handle_button_click [json]
  (if-let [chat_id json?.callback_query?.message?.chat?.id
           message_id json?.callback_query?.message?.message_id
           user_id json?.callback_query?.from?.id
           data json?.callback_query?.data
           payload (.parse JSON data)
           count payload.c
           data_user_id payload.u
           tag payload.t
           _ (= data_user_id user_id)]
    (if (= count -1)
      (eff_fetch "https://api.telegram.org/bot~TG_TOKEN~/editMessageReplyMarkup"
                 {:method "POST"
                  :body (JSON/stringify {:chat_id chat_id :message_id message_id})
                  :headers {"content-type" "application/json"}})
      (if (<= count RELOAD_LIMIT)
        (->
         (eff_fetch (str "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=~GIPHY_TOKEN~&tag=" tag))
         (e/then (fn [json] (eff_dispatch :try_handle_button_click_image [chat_id message_id count tag user_id json]))))
        (eff_fetch "https://api.telegram.org/bot~TG_TOKEN~/deleteMessage"
                   {:method "POST"
                    :body (JSON/stringify {:chat_id chat_id :message_id message_id})
                    :headers {"content-type" "application/json"}})))
    (e/pure null)))

(defn- try_handle_cat_command_send [chat_id user_id tag img_json]
  (eff_fetch
   "https://api.telegram.org/bot~TG_TOKEN~/sendVideo"
   {:method "POST"
    :body (JSON/stringify {:video img_json.data.images.original.mp4
                           :chat_id chat_id
                           :reply_markup {:inline_keyboard
                                          [[{:text (+ "Next [" RELOAD_LIMIT "]")
                                             :callback_data (.stringify JSON {:c 1 :u user_id :t tag})}
                                            {:text "Done"
                                             :callback_data (.stringify JSON {:c -1 :u user_id :t tag})}]]}})
    :headers {"content-type" "application/json"}}))

(defn- try_handle_cat_command [json]
  (if-let [text json?.message?.text
           chat_id json?.message?.chat?.id
           user_id json?.message?.from?.id
           tag (cond (.startsWith text "/cat") "cat"
                     (.startsWith text "/dog") "puppy"
                     (.startsWith text "/pig") "pig"
                     :else null)]
    (->
     (eff_fetch (str "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=~GIPHY_TOKEN~&tag=" tag) {})
     (e/then (fn [json] (eff_dispatch :try_handle_cat_command_send [chat_id user_id tag json]))))
    (e/pure null)))

(defn handle_event [key data]
  ;; (println (JSON/stringify {:key key :data data} null 2))
  (case key
    :telegram (e/batch [(try_handle_cat_command data)
                        (try_handle_button_click data)])
    :try_handle_cat_command_send (try_handle_cat_command_send (spread data))
    :try_handle_button_click_image (try_handle_button_click_image (spread data))
    (e/pure null)))

;; (defn- rate_limit_request [env json next]
;;   (let [user_id (or json?.message?.from?.id json?.callback_query?.from?.id)]
;;     (if user_id
;;       (->
;;        (.prepare env.DB "SELECT time FROM last_request_time WHERE user_id = ?1")
;;        (.bind user_id)
;;        (.first "time")
;;        (.then
;;         (fn [time]
;;           (if (> (- (Date/now) (or time 0)) 1500)
;;             (->
;;              (Promise/resolve)
;;              (.then next)
;;              (.then
;;               (fn []
;;                 (->
;;                  (.prepare env.DB "INSERT OR REPLACE INTO last_request_time (user_id, time) VALUES (?1, ?2)")
;;                  (.bind user_id (Date/now))
;;                  (.run)))))
;;             null))))
;;       null)))

;; (defn- fetch_handler [request env]
;;   (->
;;    (.json request)
;;    (.then
;;     (fn [json]
;;       (->>
;;        (.log console "[LOG] Message not handled:\n" json) (fn [])
;;        (try_handle_button_click env json) (fn [])
;;        (try_handle_cat_command env json) (fn [])
;;        (rate_limit_request env json))))
;;    (.catch console.error)
;;    (.then (fn [] (Response. "")))))

(export-default
 {:fetch
  (fn [request env ctx]
    (->
     (.json request)
     (.then (fn [update]
              (let [world (->
                           env
                           e/attach_empty_effect_handler
                           (e/attach_eff :db
                                         (fn [args]
                                           (let [sql (.at args 0) sql_args (.at args 1)]
                                             (->
                                              env.DB (.prepare sql) (.bind (spread sql_args)) .run
                                              (.then (fn [x] x.results))))))
                           (e/attach_eff :fetch
                                         (fn [args]
                                           (let [url (.at args 0) props (.at args 1)]
                                             (-> url
                                                 (.replaceAll "~TG_TOKEN~" env.TG_TOKEN)
                                                 (.replaceAll "~GIPHY_TOKEN~" env.GIPHY_TOKEN)
                                                 (fetch props)
                                                 (.then (fn [x] (.json x)))))))
                           (e/attach_eff :dispatch
                                         (fn [args]
                                           (let [key (.at args 0) data (.at args 1)]
                                             (e/run_effect (handle_event key data) world)))))]
                (e/run_effect (handle_event :telegram update) world))))
     (.catch console.error)
     (.then (fn [] (Response. (str "OK - " (Date.)))))))})
