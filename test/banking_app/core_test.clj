(ns banking-app.core-test
  (:require [clojure.test :refer :all]
            [banking-app.utils :refer [response-for]]
            [banking-app.handlers :as h]
            [banking-app.db :as db]))

(defn bank-fixture [f]
  (db/rollback-migrations)
  (db/run-migrations)
  (f)
  (db/rollback-migrations))

(use-fixtures :once bank-fixture)

(deftest test-banking-operations
  (testing "Feature 1: Creation of account"
    (let [response (:body (response-for h/create-account :post "/"
                                        {:body {:name "Mr. Black"}}))
          expected-result {"account-number" 1 , "name" "Mr. Black", "balance" 0.0}]
      (is (= response expected-result))))
  ;; Create another account for later use
  (response-for h/create-account :post "/"
                {:body {:name "Mr. White"}})
  (testing "Feature 2: View account"
    (let [account-1 (:body (response-for h/view-account :get "/account/1"
                                         {:route-params {:id 1}}))
          account-2 (:body (response-for h/view-account :get "/account/2"
                                         {:route-params {:id 2}}))
          non-existing-account (response-for h/view-account :get "/account/5"
                                             {:route-params {:id 5}})]

      (is (= account-1 {"account-number" 1 , "name" "Mr. Black", "balance" 0.0}))
      (is (= account-2 {"account-number" 2 , "name" "Mr. White", "balance" 0.0}))
      (is (= 404 (:status non-existing-account)))))
  (testing "Feature 3: Deposit money"
    (let [first-account-balance (:body (response-for (h/deposit-withdraw :+)
                                                     :post "/account/1/deposit"
                                                     {:body        {:amount 100}
                                                      :route-params {:id 1}}))
          second-account-balance (:body (response-for (h/deposit-withdraw :+)
                                                      :post "/account/2/deposit"
                                                      {:body        {:amount 90}
                                                       :route-params {:id 2}}))]
      (is (= first-account-balance {"account-number" 1 , "name" "Mr. Black", "balance" 100.0}))
      (is (= second-account-balance {"account-number" 2 , "name" "Mr. White", "balance" 90.0}))))
  (testing "Feature 4: Withdraw money"
    (let [first-account-balance (:body (response-for (h/deposit-withdraw :-)
                                                     :post "/account/1/deposit"
                                                     {:body        {:amount 5}
                                                      :route-params {:id 1}}))
          withdraw-too-much    (response-for (h/deposit-withdraw :-)
                                             :post "/account/1/deposit"
                                             {:body        {:amount 5000}
                                              :route-params {:id 1}})]
      (is (= first-account-balance {"account-number" 1 , "name" "Mr. Black", "balance" 95.0}))
      (is (= 409 (:status withdraw-too-much)))))
  (testing "Feature 5: Transfer money"
    (let [first-account-balance (:body (response-for h/transfer-money :post "/account/1/send"
                                                     {:body         {:amount 50
                                                                     :account-number 2}
                                                      :route-params {:id 1}}))
          transfer-too-much       (response-for h/transfer-money :post "/account/1/send"
                                                {:body         {:amount 5000
                                                                :account-number 2}
                                                 :route-params {:id 1}})]
      (is (= first-account-balance {"account-number" 1 , "name" "Mr. Black", "balance" 45.0}))
      (is (= 409 (:status transfer-too-much)))))
  (testing "Feature 6: Retrieve audit"
    (response-for h/transfer-money :post "/account/2/send"
                  {:body         {:amount 50
                                  :account-number 1}
                   :route-params {:id 2}})
    ;; so far in the tests,
    ;;  1. we deposited 100 to acc 1
    ;;  2. withdraw 5 from acc 1
    ;;  3. Transferred 50 to acc 2
    ;;  4. Transferred 50 to acc 1
    (let [expected-result '({"sequence" 3 "description" "recieve from #2" "credit" 50.0}
                            {"sequence" 2 "description" "send to #2" "debit" 50.0}
                            {"sequence" 1 "description" "withdraw" "debit" 5.0}
                            {"sequence" 0 "description" "deposit" "credit" 100.0})
          audit (:body (response-for h/audit :get "/account/1/audit"
                                     {:route-params {:id 1}}))]
      (is (= audit expected-result)))))
