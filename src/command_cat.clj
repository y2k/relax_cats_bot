(ns _ (:require ["./vendor/effects/effects" :as e]
                ["./vendor/effects-promise/index" :as ep]
                ["./vendor/fetch/index" :as f]))

;; (defn- try_handle_cat_command_send [chat_id user_id tag img_json]
;;   (f/fetch
;;    "https://api.telegram.org/bot~TG_TOKEN~/sendVideo"
;;    {:method :POST
;;     :decode :json
;;     :body (JSON.stringify {:video img_json.data.images.original.mp4
;;                            :chat_id chat_id
;;                            :reply_markup {:inline_keyboard
;;                                           [[{:text (+ "Next [" RELOAD_LIMIT "]")
;;                                              :callback_data (.stringify JSON {:c 1 :u user_id :t tag})}
;;                                             {:text "Done"
;;                                              :callback_data (.stringify JSON {:c -1 :u user_id :t tag})}]]}})
;;     :headers {"content-type" "application/json"}}))

;; (defn attach_fetch_keys_effect_handler [evn_atom]
;;   (swap! evn_atom
;;          (fn [env]
;;            FIXME)))

(def- RELOAD_LIMIT 3)

(defn invoke [update]
  ;; (eprintln update)

  ;; (if-let [text2 update?.message?.text
  ;;          chat_id2 update?.message?.chat?.id
  ;;          user_id2 update?.message?.from?.id
  ;;          tag2 (cond (.startsWith text2 "/cat") "cat"
  ;;                     :else nil)]
  ;;   (eprintln "1")
  ;;   (eprintln "2"))

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
      {:decode :json})
     (ep/then (fn [img_json]
                (f/fetch
                 "https://api.telegram.org/bot~TG_TOKEN~/sendVideo"
                 {:method :POST
                  :decode :json
                  :body (JSON.stringify
                         {:video img_json.data.images.original.mp4
                          :chat_id chat_id
                          :reply_markup {:inline_keyboard
                                         [[{:text (+ "Next [" RELOAD_LIMIT "]")
                                            :callback_data (.stringify JSON {:c 1 :u user_id :t tag})}
                                           {:text "Done"
                                            :callback_data (.stringify JSON {:c -1 :u user_id :t tag})}]]}})
                  :headers {"content-type" "application/json"}}))))
    (e/pure nil)))

;; (defn try_handle_cat_command [json]
;;   (if-let [text json?.message?.text
;;            chat_id json?.message?.chat?.id
;;            user_id json?.message?.from?.id
;;            tag (cond (.startsWith text "/cat") "cat"
;;                      (.startsWith text "/dog") "puppy"
;;                      (.startsWith text "/pig") "pig"
;;                      :else nil)]
;;     (ep/then
;;      (f/fetch
;;       (str "https://api.giphy.com/v1/gifs/random?rating=pg&api_key=~GIPHY_TOKEN~&tag=" tag)
;;       {:decode :json})
;;      (fn [json] (try_handle_cat_command_send chat_id user_id tag json)))
;;     (e/pure nil)))
