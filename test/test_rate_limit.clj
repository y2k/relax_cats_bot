(ns _ (:require
       ["../src/rale_limit" :as rl]
       ["../src/vendor/effects-tools/date" :as date]
       ["../src/vendor/effects-tools/state" :as state]
       ["../src/vendor/effects/effects" :as e]
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

(defn- call_effect [buffer id]
  (rl/limit
   (create_message "/cat")
   (e/thunk (fn []
              (swap! buffer (fn [xs] (conj xs id)))))))

(defn- execute_rate_limit [delay]
  (let [buffer (atom [])
        time_atom (date/create_mock (unixtime))
        w (->> {}
               (date/mock_effect_handler time_atom)
               (state/effect_handler {}))]
    ((e/then
      (call_effect buffer 1)
      (fn [_]
        (date/shift time_atom delay)
        (call_effect buffer 2)))
     w)
    (deref buffer)))

(defn- assert [expected_val actual_val]
  (let [actual (edn/to_string actual_val)
        expected (edn/to_string expected_val)]
    (if (= actual expected)
      (eprintln "OK")
      (FIXME expected " != " actual))))

(defn test []
  (assert [1 2] (execute_rate_limit 2000))
  (assert [1] (execute_rate_limit 0)))
