(ns _ (:require [".github/vendor/make/0.3.0/main" :as b]))

(b/generate
 [{:target :js
   :root "src"
   :out-dir ".github/bin/src"}
  {:target :js
   :root "test"
   :out-dir ".github/bin/test"}])