(ns edc-next.orders.creator.db)


(def state {:orders {:creator {:_supplier                "ec"
                               :min-pace                 0.1
                               :min-margin               0.05
                               :only-below-minimum?      true
                               :_only-cheaper-than-cg?   true
                               :_only-cheaper-than-ec?   false
                               :_show-make-order-dialog? false
                               :selected-categories      {"01" true
                                                          "02" true
                                                          "03" true
                                                          "04" false
                                                          "05" true
                                                          "06" true
                                                          "07" true
                                                          "08" false
                                                          "09" false
                                                          "10" false
                                                          "11" true
                                                          "12" true
                                                          "13" true
                                                          "14" false}}}})
