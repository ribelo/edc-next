(ns edc-next.ec-orders.settings.db)


(def state {:ec-orders
            {:settings
             {:_show-settings-dialog? false
              :pace-period            1
              :card-columns           2
              :product-card           {:pace               true
                                       :stock              true
                                       :margin             true
                                       :sales              false
                                       :optimal            true
                                       :rank               false
                                       :price              true
                                       :buy-price          true
                                       :cg-price           true
                                       :missing            true
                                       :profit             false
                                       :optimal-supply     false
                                       :established-margin false
                                       :volume             false
                                       :last-sale          true
                                       :last-delivery      false
                                       :promotion          false}}}})
