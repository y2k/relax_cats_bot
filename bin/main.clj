(def reload_limit 3)

(defn try_handle_button_click [next env json]
  (let [chat_id json?.callback_query?.message?.chat?.id
        message_id json?.callback_query?.message?.message_id
        user_id json?.callback_query?.from?.id
        data json?.callback_query?.data
        payload (.parse JSON data)
        count payload.c
        data_user_id payload.u]
    (if (and data (= data_user_id user_id))
      (if (= count -1)
        (fetch (+ "https://api.telegram.org/bot" env.TG_TOKEN "/editMessageReplyMarkup")
               {:method "POST"
                :body (json_to_string {:chat_id chat_id :message_id message_id})
                :headers {"content-type" "application/json"}})
        (if (<= count reload_limit)
          (->
           (fetch (+ "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=" env.GIPHY_TOKEN "&tag=cat"))
           (.then (fn [r] (.json r)))
           (.then (fn [x]
                    (let [url (+ "https://api.telegram.org/bot" env.TG_TOKEN "/editMessageMedia")
                          request {:media {:type "video" :media x.data.images.original.mp4}
                                   :chat_id chat_id
                                   :message_id message_id
                                   :reply_markup {:inline_keyboard
                                                  [[{:text (if (= reload_limit count) "Delete" (+ "Next [" (- reload_limit count) "]"))
                                                     :callback_data (.stringify JSON {:c (+ count 1) :u user_id})}
                                                    {:text "Done" :callback_data (.stringify JSON {:c -1 :u user_id})}]]}}]
                      (fetch url {:method "POST"
                                  :body (json_to_string request)
                                  :headers {"content-type" "application/json"}})))))
          (fetch (+ "https://api.telegram.org/bot" env.TG_TOKEN "/deleteMessage")
                 {:method "POST"
                  :body (json_to_string {:chat_id chat_id :message_id message_id})
                  :headers {"content-type" "application/json"}})))
      (next))))

(defn try_handle_cat_command [next env json]
  (let [message json?.message?.text
        chat_id json?.message?.chat?.id
        user_id json?.message?.from?.id]
    (if (and message (.startsWith message "/cat"))
      (->
       (fetch (+ "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=" env.GIPHY_TOKEN "&tag=cat"))
       (.then (fn [r] (.json r)))
       (.then (fn [x]
                (fetch (+ "https://api.telegram.org/bot" env.TG_TOKEN "/sendVideo")
                       {:method "POST"
                        :body (json_to_string {:video x.data.images.original.mp4
                                               :chat_id chat_id
                                               :reply_markup {:inline_keyboard
                                                              [[{:text (+ "Next [" reload_limit "]") :callback_data (.stringify JSON {:c 1 :u user_id})}
                                                                {:text "Done" :callback_data (.stringify JSON {:c -1 :u user_id})}]]}})
                        :headers {"content-type" "application/json"}}))))
      (next))))

(defn rate_limit_request [next env json]
  (let [user_id (or json?.message?.from?.id json?.callback_query?.from?.id)]
    (if user_id
      (->
       (.prepare env.DB "SELECT time FROM last_request_time WHERE user_id = ?1")
       (.bind user_id)
       (.first "time")
       (.then
        (fn [time]
          (if (> (- (Date/now) (or time 0)) 2000)
            (.then
             (next)
             (fn []
               (->
                (.prepare env.DB "INSERT OR REPLACE INTO last_request_time (user_id, time) VALUES (?1, ?2)")
                (.bind user_id (Date/now))
                (.run))))
            null))))
      null)))

(defn fetch_handler [request env context]
  (->
   (.json request)
   (.then (fn [json]
            (rate_limit_request
             (fn []
               (try_handle_cat_command
                (fn []
                  (try_handle_button_click
                   (fn []
                     (.warn console "[LOG] Message not handled:\n" json)) env json)) env json)) env json)))
   (.catch (.error console))
   (.then (fn [] (Response. "")))))

(defn json_to_string [x]
  (.stringify JSON x null 2))

(export-default {:fetch fetch_handler})
