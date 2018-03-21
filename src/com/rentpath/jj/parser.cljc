(ns com.rentpath.jj.parser
  (:require [clojure.string :as str]
            [clojure.walk :refer [postwalk]]
            #?(:clj [instaparse.core :as insta]
               :cljs [instaparse.core :as insta :refer-macros [defparser]])
            [com.rentpath.jj.lang :as lang]))

(def grammar
  "
  jj = <WS*> jjValue (<WS*> jjValue <WS*>)* <WS*>

  jj_ = <WS*> jjValue (<WS*> WITH <WS*> jjValue)* <WS*>

  <jjValue> = jjSymbol | jjNumber | jjString | jjObject | jjArray | jjFalse | jjNull | jjTrue | WITH

  jjTrue = 'true'

  jjFalse = 'false'

  jjNull = 'null'

  jjNumber = NUMBER

  jjString = STRING

  jjSymbol = SYMBOL

  jjMember = MEMBER

  <jjObject> = (jjEmptyObject | jjObject_curly | jjObject_plain | jjObject_smart)

  jjObjectUp = LEVEL_UP

  jjObjectDown =  LEVEL_DOWN

  jjObject_smart = (jjMember (<WS*> (jjObjectUp | jjObjectDown) <WS*> jjMember)*)

  jjObject_plain = (jjMember (<WS*> <','?> <WS*> jjMember)*)

  jjObject_curly = <'{'> <WS*> jjObject_plain <WS*> <'}'>

  jjEmptyObject = <'{'> <WS*> <'}'>

  jjArray = <'['> <WS*> (jjValue (<WS*> jjValue)*)? <WS*> <']'>

  WITH = <'#w'>

  <LEVEL_UP> = <'#'> #\"<+\"

  <LEVEL_DOWN_QUERY> = #\"[^>]+\"

  <LEVEL_DOWN> = <'#'> !('<'|'w') LEVEL_DOWN_QUERY '>'

  <MEMBER> = (jjSymbol | jjString) <WS*> <':'> <WS*> jjValue

  <NUMBER> = #\"-?(0|([1-9][0-9]*))(\\.[0-9]+)?([eE][+-]?[0-9]+)?\"

  <WS> = ' ' | '\\t' | '\\n' | '\\r'

  <SYMBOL> = #\"([^\\\"\\\\:\\s,\\[\\]\\{\\}\\(\\)#<>]|\\\\([\\\"\\\\/nfnrt])|(u[0-9a-fA-F]{4}))+\"

  <SYMBOL_> = #\"([^\\\"\\\\\\s]|\\\\([\\\"\\\\/nfnrt])|(u[0-9a-fA-F]{4}))*\"

  <STRING> = #\"\\\"([^\\\"\\\\]|\\\\([\\\"\\\\/nfnrt])|(u[0-9a-fA-F]{4}))*\\\"\"
  ")

#?(:clj (def parser
          (insta/parser grammar)))

#?(:cljs (defparser parser grammar))



;; ;; Specs for jj value tags in the AST
;; (s/def ::jjNumber      #{:jjNumber})
;; (s/def ::jjString      #{:jjString})
;; (s/def ::jjSymbol      #{:jjSymbol})
;; (s/def ::jjObject      #{:jjObject})
;; (s/def ::jjArray       #{:jjArray})
;; (s/def ::jjFalse       #{:jjFalse})
;; (s/def ::jjTrue        #{:jjTrue})
;; (s/def ::jjNull        #{:jjNull})
;; (s/def ::jjEmptyObject #{:jjEmptyObject})

;; (s/def ::string-in-string (s/and string?
;;                                  #(str/starts-with? % "\"")
;;                                  #(str/ends-with? % "\"")))

#?(:clj
   (defn parse-number
     [[tag num-str :as arg]]
     (let [num (try
                 (Long/parseLong num-str)
                 (catch NumberFormatException _
                   (try
                     (Double/parseDouble num-str))))]
       num
       #_(s/assert number? num))))

#?(:cljs
   (defn parse-number
     [[tag num-str :as arg]]
     (if (re-find #"\." num-str)
       (js/parseFloat num-str)
       (js/parseInt num-str "10"))))

;; (s/fdef
;;  parse-number
;;  :args (s/and (s/cat :arg (s/tuple ::jjNumber string?)))
;;  :ret number?)

(defn parse-string
  [[tag str :as arg]]
  (let [;; str (s/assert ::string-in-string str)
        str-length (count str)]
    (subs (subs str 1) 0 (- str-length 2))))

;; (s/fdef
;;  parse-string
;;  :args (s/and (s/cat :arg (s/tuple ::jjString ::string-in-string)))
;;  :fn #(= 2 (- (count (-> % :args :arg 1)) (count (:ret %))))
;;  :ret string?)

(defn parse-symbol
  [[tag str]]
  (let [sym (symbol (str/trim str))]
    (get lang/*reserved-symbols* sym sym)))

(defn parse-array
  [[tag & things]]
  (into [] things))

(defn parse-member
  [[tag k v]]
  [k v])

(defn parse-object
  [[tag & entries]]
  (into {} entries))

(defn member->map
  [m member]
  (if (and (coll? member)
           (= (first member) :jjMember))
    (let [[tag k v] member]
      (assoc m k (member->map {} v)))
    member))

(defn object->map
  [m [tag & members]]
  (reduce
   (fn [m member]
     (merge m (member->map {} member)))
   m
   members))

(defn analyze
  "Perform data-type parsing and flatten the AST."
  [ast]
  (postwalk
   (fn [x]
     (if (coll? x)
       (let [tag (first x)]
         (case tag
           :jjArray        (parse-array  x)
           :jjEmptyObject  {}
           :jjFalse        false
           :jjMember       (parse-member x)
           :jjNumber       (parse-number x)
           :jjObject       (parse-object x)
           :jjObject_plain (parse-object x)
           :jjObject_curly (parse-object x)
           :jjObject_smart (parse-object x)
           :jjString       (parse-string x)
           :jjSymbol       (parse-symbol x)
           :jjTrue         true
           :jjNull         nil
           :WITH           nil ;; for now
           x))
       x))
   ast))

(defn parse
  "Code can consist of multiple jj programs."
  [s]
  (if (empty? s)
    nil
    (let [parsed (parser s)]
      (if (instance? instaparse.gll.Failure parsed)
        (throw (ex-info "Failed to parse your jj program." {:failure parsed}))
        (into [] (comp (map analyze)
                       (keep identity))
              (next parsed))))))
