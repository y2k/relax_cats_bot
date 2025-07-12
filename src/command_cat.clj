(ns _ (:require ["./vendor/effects/effects" :as e]
                ["./vendor/effects-promise/index" :as io]
                ["./vendor/fetch/index" :as f]))

(def- RELOAD_LIMIT 3)

(defn try_handle_cat_command [update]
  (if-let [text update?.message?.text
           chat_id update?.message?.chat?.id
           user_id update?.message?.from?.id
           tag (cond (.startsWith text "/cat") "cat"
                     (.startsWith text "/dog") "puppy"
                     (.startsWith text "/pig") "pig"
                     :else nil)]
    (->
     (f/fetch
      (str "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=~GIPHY_TOKEN~&tag=" tag)
      {:decoder :json
       :mapper (fn [r] r.data.images.original.mp4)})
     (io/then (fn [mp4_url]
                ;; (eprintln __LOC__ mp4_url)
                (f/fetch
                 "https://api.telegram.org/bot~TG_TOKEN~/sendVideo"
                 {:method :POST
                  :body (JSON.stringify
                         {:video mp4_url
                          :chat_id chat_id
                          :reply_markup {:inline_keyboard
                                         [[{:text (+ "Next [" RELOAD_LIMIT "]")
                                            :callback_data (.stringify JSON {:c 1 :u user_id :t tag})}
                                           {:text "Save"
                                            :callback_data (.stringify JSON {:c -1 :u user_id :t tag})}]]}})
                  :headers {"content-type" "application/json"}}))))
    (io/pure nil)))

(defn try_handle_button_click [update]
  (if-let [chat_id update?.callback_query?.message?.chat?.id
           message_id update?.callback_query?.message?.message_id
           user_id update?.callback_query?.from?.id
           data update?.callback_query?.data
           payload (JSON.parse data)
           count payload.c
           data_user_id payload.u
           tag payload.t
           _ (= data_user_id user_id)]
    (if (= count -1)
      (f/fetch "https://api.telegram.org/bot~TG_TOKEN~/editMessageReplyMarkup"
               {:method "POST"
                :body (JSON.stringify {:chat_id chat_id :message_id message_id})
                :headers {"content-type" "application/json"}})
      (if (<= count RELOAD_LIMIT)
        (io/then
         (f/fetch (str "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=~GIPHY_TOKEN~&tag=" tag)
                  {:decoder :json
                   :mapper (fn [r] r.data.images.original.mp4)})
         (fn [mp4_url]
           (f/fetch "https://api.telegram.org/bot~TG_TOKEN~/editMessageMedia"
                    {:method "POST"
                     :body (JSON.stringify
                            {:media {:type "video" :media mp4_url}
                             :chat_id chat_id
                             :message_id message_id
                             :reply_markup {:inline_keyboard
                                            [[{:text (if (= RELOAD_LIMIT count) "Delete" (+ "Next [" (- RELOAD_LIMIT count) "]"))
                                               :callback_data (JSON.stringify {:c (+ count 1) :u user_id :t tag})}
                                              {:text "Save" :callback_data (JSON.stringify  {:c -1 :u user_id :t tag})}]]}})
                     :headers {"content-type" "application/json"}})))
        (f/fetch "https://api.telegram.org/bot~TG_TOKEN~/deleteMessage"
                 {:method "POST"
                  :body (JSON.stringify {:chat_id chat_id :message_id message_id})
                  :headers {"content-type" "application/json"}})))
    (io/pure nil)))
