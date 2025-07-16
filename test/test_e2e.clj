(ns _ (:require
       ["../src/command_cat" :as cat]
       ["../src/date" :as date]
       ["../src/main" :as main]
       ["../src/state" :as state]
       ["../src/rale_limit" :as rl]
       ["../src/vendor/effects-promise/index" :as io]
       ["../src/vendor/effects/effects" :as e]
       ["../src/vendor/fetch/index" :as f]
       ["./vendor/edn/main" :as edn]))

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

(defn- assert [expected_val actual_val]
  (let [actual (edn/to_string actual_val)
        expected (edn/to_string expected_val)]
    (if (= actual expected)
      (eprintln "OK")
      (FIXME "test failed...\n\n" expected "\n\n!=\n\n" actual "\n"))))

(defn test []
  (let [requests (atom [])
        env {:fetch:fetch (fn [x]
                            (swap! requests (fn [xs] (conj xs x)))
                            ((io/pure {:data {:images {:original {:mp4 "img"}}}}) nil))}]
    (.then
     (first
      ((cat/try_handle_cat_command (create_message "/cat")) env))
     (fn []
       (let [expected (edn/to_string
                       {:url "https://api.telegram.org/bot~TG_TOKEN~/sendVideo"
                        :props {:method :POST
                                :body "{\"video\":{\"data\":{\"images\":{\"original\":{\"mp4\":\"img\"}}}},\"chat_id\":241854720,\"reply_markup\":{\"inline_keyboard\":[[{\"text\":\"Next [3]\",\"callback_data\":\"{\\\"c\\\":1,\\\"u\\\":241854720,\\\"t\\\":\\\"cat\\\"}\"},{\"text\":\"Save\",\"callback_data\":\"{\\\"c\\\":-1,\\\"u\\\":241854720,\\\"t\\\":\\\"cat\\\"}\"}]]}}"
                                :headers {"content-type" "application/json"}}})
             actual (edn/to_string (last (deref requests)))]
         (assert expected actual))))))
