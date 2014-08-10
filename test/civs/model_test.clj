(ns civs.model-test
  (:require [clojure.test :refer :all]
            [civs.core :refer :all]
            [civs.model :refer :all]
            [civs.logic :refer :all]
            [civs.logic.basic :refer :all]
            [civs.logic.demographics :refer :all]
            [civs.logic.tribe-choices :refer :all])
  (:import [civs.model Population Tribe Town]))

(def w77 (load-world "examples-worlds/seed_77.world"))

(deftest test-total-persons
  (is (= 15 (total-persons (Population. 1 2 3 4 5)))))

(deftest test-active-persons
  (is (= 5 (active-persons (Population. 1 2 3 4 5)))))

(deftest test-is-dead?
  (is (= false (is-dead? (Tribe. nil nil nil (Population. 1 2 3 4 5) nil))))
  (is (= true  (is-dead? (Tribe. nil nil nil (Population. 0 0 0 0 0) nil)))))

(deftest test-alive?
  (is (= true  (alive?  (Tribe. nil nil nil (Population. 1 2 3 4 5) nil))))
  (is (= false (alive?  (Tribe. nil nil nil (Population. 0 0 0 0 0) nil)))))

(deftest test-create-tribe
  (let [g (create-game nil)
        g (:game (create-tribe g "Tribe1" nil nil nil))
        t (get (-> g .tribes) 1)]
  (is t)
  (is (= 1 (.id t)))))

(deftest test-cells-around
  (is (= '({:x 4, :y 7} {:x 4, :y 8} {:x 4, :y 9} {:x 5, :y 7} {:x 5, :y 8} {:x 5, :y 9} {:x 6, :y 7} {:x 6, :y 8} {:x 6, :y 9})
        (cells-around w {:x 5 :y 8} 1))))

(deftest test-cells-around-near-borders
  (is (= '( {:x 0, :y 0} {:x 0, :y 1} {:x 1, :y 0} {:x 1, :y 1})
        (cells-around w {:x 0 :y 0} 1))))

(deftest test-land-cells-around-in-the-ocean
  ; We are in a corner, surrounded just by see
  (is '() (land-cells-around w77 {:x 0 :y 0} 5)))

(deftest test-land-cells-around-in-land
  ; This tile is land
  (is '({:x 100, :y 100}) (land-cells-around w77 {:x 100 :y 100} 0)))

(deftest test-create-tribe
  (let [initial-g (create-game nil)
        res (create-tribe initial-g "name" {:x 15 :y 18} (Population. 1 2 3 4 5) initial-culture)
        g (:game res)
        t (:tribe res)]
    ; there should be one tribe in the world
    (= 1 (.size (tribes g)))
    (= 1 (.id t))
    (= "name" (.name t))
    (= {:x 15 :y 18} (.position t))
    (= (Population. 1 2 3 4 5) (.population t))
    (= initial-culture (.culture t))))

(deftest test-create-town
  (let [initial-g (create-game nil)
        res (create-town initial-g "name" {:x 15 :y 18} :123)
        g (:game res)
        t (:town res)]
    ; there should be one town in the world
    (= 1 (.size (towns g)))
    (= 1 (.id t))
    (= "name" (.name t))
    (= {:x 15 :y 18} (.position t))
    (= :123 (.owner t))))

(deftest test-game-total-pop
  (let [g0 (create-game nil)
        g1 (:game (create-tribe g0 "name" {:x 15 :y 18} (Population. 1 2 3 4 5) initial-culture))
        g2 (:game (create-tribe g1 "name" {:x 15 :y 18} (Population. 0 1 2 0 0) initial-culture))]
    (= 0 (game-total-pop g0))
    (= 15 (game-total-pop g1))
    (= 18 (game-total-pop g2))))

(deftest test-get-tribe
  (let [g0 (create-game nil)
        g1 (:game (create-tribe g0 "name" {:x 15 :y 18} (Population. 1 2 3 4 5) initial-culture))]
    (= nil (get-tribe g0 1))
    (= (Tribe. 1 "name" {:x 15 :y 18} (Population. 1 2 3 4 5) initial-culture) (get-tribe g1 1))))

(deftest test-get-town
  (let [g0 (create-game nil)
        g1 (:game (create-town g0 "name" {:x 15 :y 18} :123))]
    (= nil (get-town g0 1))
    (= (Town. 1 "name" {:x 15 :y 18} :123) (get-town g1 1))))

(deftest test-ghost-city
  (let [g0 (create-game nil)
        g1 (:game (create-tribe g0 "name" {:x 15 :y 18} (Population. 1 2 3 4 5) initial-culture))
        g2 (:game (create-town g1 "name" {:x 15 :y 18} 1))
        g3 (:game (create-tribe g2 "name" {:x 15 :y 18} (Population. 0 0 0 0 0) initial-culture))
        g4 (:game (create-town g3 "name" {:x 15 :y 18} 3))]
    (= false (ghost-city? g2 2))
    (= true (ghost-city? g4 4))))

(deftest test-n-tribes-alive
  (let [g0 (create-game nil)
        g1 (:game (create-tribe g0 "name" {:x 15 :y 18} (Population. 1 2 3 4 5) initial-culture))
        g2 (:game (create-tribe g1 "name" {:x 15 :y 18} (Population. 0 0 0 0 0) initial-culture))]
    (= 0 (n-tribes-alive g0))
    (= 1 (n-tribes-alive g1))
    (= 1 (n-tribes-alive g2))))

(deftest test-update-tribe
  (let [g0 (create-game nil)
        g1 (:game (create-tribe g0 "name" {:x 15 :y 18} (Population. 1 2 3 4 5) initial-culture))
        g2 (update-tribe g1 (Tribe. 1 "name2" {:x 25 :y 28} (Population. 0 1 1 0 0) initial-culture))]
    (= (Tribe. 1 "name"  {:x 15 :y 18} (Population. 1 2 3 4 5) initial-culture) (get-tribe g1 1))
    (= (Tribe. 1 "name2" {:x 25 :y 28} (Population. 0 1 1 0 0) initial-culture) (get-tribe g2 1))))

(deftest test-n-ghost-cities
  (let [g0 (create-game nil)
        g1 (:game (create-tribe g0 "name" {:x 15 :y 18} (Population. 1 2 3 4 5) initial-culture))
        g2 (:game (create-town g1 "name" {:x 15 :y 18} 1))
        g3 (:game (create-tribe g2 "name" {:x 15 :y 18} (Population. 0 0 0 0 0) initial-culture))
        g4 (:game (create-town g3 "name" {:x 15 :y 18} 3))]
    (= 0 (n-ghost-cities g0))
    (= 0 (n-ghost-cities g2))
    (= 1 (n-ghost-cities g4))))