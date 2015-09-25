(ns summary-and-spelling-checker.models.summary-extractor
  (:import (java.io BufferedReader FileReader))
  (:import java.io.File)
  (:refer-clojure :exclude [* - + == /])
  (:require [clojure.java.io :refer (file)]
            [clojure.string :refer (split)]
            [clojure.core.matrix :refer :all]
            [clojure.core.matrix.operators :refer :all]))

(def ^:private ^:const folder-with-txt-data "txtdata")
(def ^:private ^:const txt1-path "txtdata/tekst1.txt")
(def ^:private ^:const txt2-path "txtdata/tekst2.txt")
(def ^:private ^:const txt3-path "txtdata/tekst3.txt")

(defn- word-in-nb-documents 
  "Counts the number of occurrences of word in the documents"
  [word documents]
  (->> documents
    (filter #(contains?  % word))count))

(defn- idf-words 
  "Computs inverse document frequency - IDF for words"
  [documents]
  (let [N (count documents)
        all-docs-words (apply clojure.set/union
                              (mapv set documents))]
    (->> all-docs-words
      (map (fn[w]  {w (Math/log (/ N
                                   (word-in-nb-documents w
                                                         documents)))}))
      (into {}))))


(defn- sentences-in-document
  "Extracts the sentences in a particular document"
  [file]
  (with-open [rdr (BufferedReader. (FileReader.  file))] 
    (->> (line-seq rdr)
         (map clojure.string/trim)
         (mapcat #(split % #"[.|!|?|:]+"))
         (mapv clojure.string/lower-case))))

(defn- gen-docs-w-sentences
  "Takes a path and generates a vector of a vector of sentences, 
  as per the documents containing them"
  [path]
  (let [files (.listFiles (File. path))]
    (mapv sentences-in-document 
          files)))

(defn- words-in-document-sentences
  "Extracts the words from all document sentences "
  [document-sentences] 
  (mapcat #(split % #"[\s|,|;]") document-sentences ))

(defn- gen-idf-map-from-docs-sentences
  "Generates a map with all words inverse document frequency"
  [documents-w-sentences]
  (let [documents-w-words (map set (map words-in-document-sentences documents-w-sentences))]
    (idf-words documents-w-words)))

(defn- tfidf-vector-from-sentence
  "Generates term frequency inverse document frequency from sentence"
  [idf-map sentence]
  (let [sentence-words (split sentence #"[\s|,|;|:]+")
        tf-sentence  (->> sentence-words
                          (map (fn [k] {k 1}))
                          (reduce (partial merge-with +)))]
    (->> tf-sentence
         (map (fn [[k v]] { k (* v (get idf-map k))} ))
         (into {}))))

(defn- cosine-similarity
  "Computes cosine similarity for two sentences in order to get information
  about the angle between the two vectors because the most
  salient sentences are those with the higher degrees"
  [tfidf-sentence1 tfidf-sentence2]
  (let [common-words (clojure.set/intersection (set (keys tfidf-sentence1))
                                               (set (keys tfidf-sentence2)))
        s1-common (select-keys tfidf-sentence1 common-words)
        s2-common (select-keys tfidf-sentence2 common-words)

        Ss1Xs2 (reduce + (vals  (merge-with * s1-common s2-common)))
        sqrt-Ss1pow2 (->> (vals tfidf-sentence1)
                          (map  #(Math/pow % 2))
                          (reduce +  )
                          Math/sqrt)
       
        sqrt-Ss2pow2 (->> (vals tfidf-sentence2)
                          (map  #(Math/pow % 2))
                          (reduce +  )
                          Math/sqrt)]
    (if (every? (comp not zero?)
                [sqrt-Ss1pow2 sqrt-Ss2pow2] )
      (/ Ss1Xs2 (* sqrt-Ss1pow2 sqrt-Ss2pow2))
      0)))


(defn- power-method
  "Resolves the previous equation using iterative method called the power-method"
  [mat error]
  (let [size (dimension-count mat 0)]
    (loop [p (matrix (into [] (repeat size
                                      (/ 1 size))))]
      (let [new-p (mmul (transpose mat) p)
            sigma (distance new-p p)]
        (if (< sigma error)
          new-p
          (recur new-p))))))

(defn- lexrank
  "Using previous functions represents sentences as term frequency inverse document frequency vectors, 
   builds centrality raw matrix that represents relationship between each pair of sentences and 
   if similarity is higher than 0.2 puts it into corresponding sentences. After that computes degrees for every row,
   applies power method and shows topN sentences with highest LexRank Scores"
  [path topN]
  (let [sentences-by-docs (gen-docs-w-sentences path)
        idf-map (gen-idf-map-from-docs-sentences
                 sentences-by-docs)
        all-sentences (into [] (mapcat  identity sentences-by-docs))
        sentences-w-tfidf (into [] (reduce concat
                                           (for [s sentences-by-docs]
                                             (map (partial tfidf-vector-from-sentence
                                                           idf-map)
                                                  s))))
        cent-raw-matrix (matrix  (into [] (for [i sentences-w-tfidf]
                                            (into [] (for [j sentences-w-tfidf]
                                                       (let [cos-sim-i-j
                                                             (cosine-similarity  i j)]
                                                         (if (>= cos-sim-i-j
                                                                 0.2)
                                                           cos-sim-i-j
                                                           0)))))))
        degrees (->> (rows cent-raw-matrix)
                   (mapv (partial reduce +)))
        centrality-matrix (matrix  (into []
                                         (for [i (range (count degrees))]
                                           (/  (get-row cent-raw-matrix i)
                                               (get degrees i)))))
        lexrank-vector (power-method centrality-matrix 0.1)
        lexrank-v-w-indices (zipmap (iterate inc 0) lexrank-vector)]
    (->> (sort-by val > lexrank-v-w-indices)
         (take topN)
         (map #(get % 0))
         (map #(get all-sentences %)))))


(defn get-thesis
  "Inserts entered texts in appropriate textual files and calls lexrank function. "
  [text1 text2 text3 numberOfThessis]
  (spit txt1-path text1)
  (spit txt2-path text2)
  (spit txt3-path text3)
  (lexrank folder-with-txt-data (read-string numberOfThessis)))