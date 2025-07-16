(ns _ (:require
       ["./test_rate_limit" :as rl]
       ["./test_e2e" :as e2e]))

(rl/test)
(e2e/test)
