(require [vendor.effects :as e])

(defn- eff_fetch [url props] (e/call :fetch [url props]))
(defn- eff_db    [db]        (e/call :db    db))

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
        (e/broadcast :try_handle_button_click_image
                     (eff_fetch (str "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=~GIPHY_TOKEN~&tag=" tag))
                     (fn [json] [chat_id message_id count tag user_id json]))
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
    (e/broadcast :try_handle_cat_command_send
                 (eff_fetch (str "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=~GIPHY_TOKEN~&tag=" tag) {})
                 (fn [json] [chat_id user_id tag json]))
    (e/pure null)))

(defn- delete_message_welcome_message [json]
  (if-let [chat_id json?.result?.chat?.id
           message_id json?.result?.message_id]
    (e/fork
     (e/seq (e/sleep 30)
            (eff_fetch "https://api.telegram.org/bot~TG_TOKEN~/deleteMessage"
                       {:method "POST"
                        :body (JSON/stringify {:chat_id chat_id :message_id message_id})
                        :headers {"content-type" "application/json"}})))
    (e/pure null)))

(defn- try_handle_new_user_end [chat_id message_id user img_json cas_json]
  (let [video_fx (eff_fetch
                  "https://api.telegram.org/bot~TG_TOKEN~/sendVideo"
                  {:method "POST"
                   :body (JSON/stringify
                          {:video img_json.data.images.original.mp4
                           :chat_id chat_id
                           :parse_mode :MarkdownV2
                           :caption
                           (let [username (str "[" user.name "](tg://user?id=" user.id ")")]
                             (if cas_json.ok
                               (str "Админ, забань " username " - он точно спамер!!! [Пруф](https://cas.chat/query?u=" user.id ")")
                               (str username ", докажите что вы человек\nНапишите что происходит на картинке, у вас 30 секунд 😸")))})
                   :headers {"content-type" "application/json"}})]
    (if cas_json.ok
      video_fx
      (e/broadcast :welcome_screen_sended video_fx (fn [r] [r])))))

(defn- try_handle_new_user [json]
  (if-let [user_id json?.message?.new_chat_member?.id
           user {:name json?.message?.new_chat_member?.first_name
                 :id user_id}
           message_id json?.message?.message_id
           chat_id json?.message?.chat?.id]
    (e/broadcast :try_handle_new_user_end
                 (e/seq (eff_fetch "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=~GIPHY_TOKEN~&tag=cat" {})
                        (eff_fetch (str "https://api.cas.chat/check?user_id=" user_id) {}))
                 (fn [r] [chat_id message_id user (get r 0) (get r 1)]))
    (e/pure null)))

(defn- handle_rate_limit [data]
  (if-let [user_id (or data?.update?.message?.from?.id data?.update?.callback_query?.from?.id)
           _ (> (- data.now (or (get data.db user_id) 0)) 1500)]
    (e/seq (eff_db (assoc data.db user_id data.now))
           (e/dispatch :telegram data.update))
    (e/pure null)))

(defn handle_event [key data]
  ;; (println (JSON/stringify {:key key :data data} null 2))
  (case key
    :raw_telegram (handle_rate_limit data)
    :telegram (e/batch [(try_handle_cat_command data)
                        (try_handle_button_click data)
                        (try_handle_new_user data)])
    :welcome_screen_sended (delete_message_welcome_message (spread data))
    :try_handle_cat_command_send (try_handle_cat_command_send (spread data))
    :try_handle_button_click_image (try_handle_button_click_image (spread data))
    :try_handle_new_user_end (try_handle_new_user_end (spread data))
    (e/pure null)))

;; Prelude Begin

(defn atom [x] (Array/of x))
(defn reset [a x]
  (.pop a)
  (.push a x))
(defn deref [a] (get a 0))

;; Prelude End

(def GLOBAL_REQUEST_TIMES (atom {}))

(export-default
 {:fetch
  (fn [request env ctx]
    (->
     (.json request)
     (.then (fn [update]
              (let [world (->
                           env
                           e/attach_empty_effect_handler
                           (e/attach_eff :sleep
                                         (fn [timeout]
                                           (Promise. (fn [resolve] (setTimeout resolve (* 1000 timeout))))))
                           (e/attach_eff :fork
                                         (fn [fx]
                                           (.waitUntil ctx (fx world))
                                           (Promise/resolve null)))
                           (e/attach_eff :db
                                         (fn [db]
                                           (reset GLOBAL_REQUEST_TIMES db)
                                           (Promise/resolve null)))
                           (e/attach_eff :fetch
                                         (fn [args]
                                           (let [url (.at args 0) props (.at args 1)]
                                             (-> url
                                                 (.replaceAll "~TG_TOKEN~" env.TG_TOKEN)
                                                 (.replaceAll "~GIPHY_TOKEN~" env.GIPHY_TOKEN)
                                                 (fetch props)
                                                 (.then (fn [x] (.json x)))
                                                 (.then (fn [r] (println "FETCH: " r) r))))))
                           (e/attach_eff :dispatch
                                         (fn [args]
                                           (let [key (.at args 0) data (.at args 1)]
                                             (e/run_effect (handle_event key data) world)))))]
                (e/run_effect (handle_event :raw_telegram {:update update
                                                           :now (Date/now)
                                                           :db (deref GLOBAL_REQUEST_TIMES)}) world))))
     (.catch console.error)
     (.then (fn [] (Response. (str "OK - " (Date.)))))))})
