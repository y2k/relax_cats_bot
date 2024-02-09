(import [fs.promises :as fs])
(require [vendor.effects :as e]
         [main :as app])

(defn- assert [path]
  (->
   (fs/readFile (.replace path "/input/" "/output/") "utf-8")
   (.catch (fn [] null))
   (.then
    (fn [log_json]
      (->
       (fs/readFile path "utf-8")
       (.then
        (fn [event_json]
          (let [event (JSON/parse event_json)
                fx_log []
                log (.reverse (JSON/parse (or log_json "[]")))]
            (defn- test_eff_handler [name args]
              (if (= :fork name)
                (args {:perform test_eff_handler})
                (if (> log.length 0)
                  (let [x (.pop log)]
                    (if (= (JSON/stringify [x.key x.data]) (JSON/stringify [name args]))
                      (Promise/resolve x.out)
                      (FIXME "Log: " (.replace path "/input/" "/output/") "\n" (JSON/stringify [x.key x.data]) "\n!=\n" (JSON/stringify [name args]) "\n")))
                  (do
                    (.push fx_log {:key name :data args})
                    (Promise/resolve null)))))
            (->
             (app/handle_event event.key event.data)
             (e/run_effect {:perform test_eff_handler})
             (.then (fn []
                      (if (= log_json null)
                        (fs/writeFile (.replace path "/input/" "/output/") (JSON/stringify fx_log null 4))
                        (if (= log.length 0) null
                            (FIXME "Log not consumed: " path "\n" (JSON/stringify (.toReversed log) null 2)))))))))))))))

(let [path "../test/samples/input/"]
  (->
   (fs/readdir path)
   (.then
    (fn [files]
      (->
       files
       (.filter (fn [name] (.test (RegExp. "\\.json$") name)))
       (.map (fn [name] (assert (str path name)))))))))
