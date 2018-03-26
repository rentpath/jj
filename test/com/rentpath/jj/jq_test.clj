(ns com.rentpath.jj.jq-test
  (:require [com.rentpath.jj.jq :as sut]
            [clojure.test :as t]))

(def example-json
  "From Dialogflow example."
  {"id" "ac1a3ed7-e257-482d-80b8-0b9cecadd88f"
   "timestamp" "2018-01-01T00:00:00.000Z"
   "lang" "en"
   "result" {"score" 0.9100000262260437
             "resolvedQuery" "add foo"
             "action" "item.add"
             "metadata" {"intentId" "8d550f05-a694-4d57-b925-etc-etc"
                         "webhookUsed" "true"
                         "webhookForSlotFillingUsed" "false"
                         "intentName" "Add item"}
             "actionIncomplete" false
             "fulfillment" {"speech" ""
                            "messages" [{"type" 0
                                         "speech" ""}]}
             "source" "agent"
             "parameters" {"item" "foo"}
             "contexts" []}
   "status" {"code" 200,
             "errorType" "success"
             "webhookTimedOut" false}
   "sessionId" "98c6f565-d33f-4ba9-bddd-etc-etc"
   "alternativeResultsFromKnowledgeService" {}})

(t/deftest test-jq
  (t/are [json query result] (= result (sut/jq json query))
    {:ids [1 2 3 4]} ".ids" [[1 2 3 4]]
    example-json ".result.metadata.intentName" ["Add item"]
    example-json ".status.code | . - 1" [199]
    example-json ".result.fulfillment.messages | length" [1]
    example-json ".result.fulfillment.messages | map( .type )" [[0]]))
