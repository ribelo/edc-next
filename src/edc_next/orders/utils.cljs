(ns edc-next.orders.utils)

(def animal-names
  ["aligator"
   "agama"
   "baran"
   "bizon"
   "chomik"
   "delfin"
   "emu"
   "fredka"
   "gęś"
   "gazela"
   "hipopotam"
   "indyk"
   "jeż"
   "krowa"
   "koza"
   "lama"
   "mors"
   "nietperz"
   "niedźwiedź"
   "dźwiedź"
   "toparz"
   "owca"
   "prosie"
   "ryś"
   "ryjówka"
   "słoń"
   "tygrys"
   "uklejka"
   "wilk"
   "york"
   "zebra"])


(defn random-animal []
  (rand-nth animal-names))


(random-animal)