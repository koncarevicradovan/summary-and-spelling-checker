(ns summary-and-spelling-checker.models.spelling_corrector
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(use '[clojure.set :only (union)])
(use 'clojure.java.io)

(def ^:private ^:const token-regex #"[a-z']+")

(defn- words [text] 
  "Using a regular expression for tokenizing 
   a string into words."
  (re-seq token-regex (string/lower-case text)))

(defn- train [feats]
  "Training data structure consists of words and their frequencies."
  (frequencies feats))

(def ^:private n-words 
  (train (words
           (slurp "data/big.txt"))))

(def ^:private ^:const alphabet
  "abcdefghijklmnopqrstuvwxyz")

; We are defining 5 operations for 

(defn- split-word [word i] 
  "Splits the word in two parts, with length of first part equal to i"
  [(.substring word 0 i) (.substring word i)])

(defn- delete-char [[w1 w2]]
  "Removes one character from the word to get word variations."
  (str w1 (.substring w2 1)))

(defn- transpose-split [[w1 w2]]
  "Transposes two characters get word variations."
  (str w1 (second w2) (first w2) (.substring w2 2)))

(defn- replace-split [[w1 w2]]
  "Replaces one letter by another character from the alphabet
   that we defined as private constant vector."
  (let [w2-0 (.substring w2 1)]
    (map #(str w1 % w2-0) alphabet)))

(defn- replace-split-extended [[w1 w2]]
  "Replaces one letter by apostrophe."
  (let [w2-0 (.substring w2 1)]
    (map #(str w1 % w2-0) "'")))

(defn- replace-split-extended2 [[w1 w2]]
  "Connescts two words with dash"
  (let [w2-0 w2]
    (map #(str w1 % w2-0) "-")))

(defn- insert-split [[w1 w2]]
  "Inserts a character into the word"
  (map #(str w1 % w2) alphabet))

(defn- edits-1 [word]
  "Calculates all of the possible variations of the word 
   using operations defined above"
  (let [splits (map (partial split-word word)
                    (range (inc (count word))))
        long-splits (filter #(> (count (second %)) 1) 
                            splits)
        deletes (map delete-char long-splits)
        transposes (map transpose-split long-splits)
        replaces1 (mapcat replace-split long-splits)
        replaces2 (mapcat replace-split-extended long-splits)
        replaces3 (mapcat replace-split-extended2 long-splits)
        inserts (remove nil? (mapcat insert-split splits))]
    (set (concat deletes transposes
                 replaces1 replaces2 replaces3 inserts))))

(defn- known-edits-2 [word]
  "Using edits-1 function to get the edits of the edits of a word, 
   but only if they exist in the training set"
  (set (filter (partial contains? n-words)
               (apply union
                      (map #(edits-1 %)
                           (edits-1 word))))))

(defn- known [words]
  "From sequence of words returns only words that exist
   in the training dataset"
  (set (filter (partial contains? n-words) words)))

(defn correct_word [word]
  "Takes all of the words variations made by functions above an
    returns the frequently seen in training dataset."
  (let [candidate-thunks [#(known (list word))
                          #(known (edits-1 word))
                          #(known-edits-2 word)
                          #(list word)]]
    (->>
      candidate-thunks
      (map (fn [f] (f)))
      (filter #(> (count %) 0))
      first
      (map (fn [w] [(get n-words w 1) w]))
      (reduce (partial max-key first))
      second)))

(defn correct_text [text]
  "Using word correction in order to correct text. After splitting text 
   into words function correct_word is mapped to that words"
  (apply str 
         (interpose " " (map correct_word (words text)))))

(defn improve_training_txt [text]
  "Improving training dataset ie textual file, but only if size of
  file is less than 100 Mb"
  (if (< (.length (io/file "data/big.txt")) 1304300)
    (spit "data/big.txt"
          (str text " ")
          :append true)))