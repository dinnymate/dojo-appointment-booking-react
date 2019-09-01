(ns dojo-appointment.client.style
  (:require [polished :refer [parseToHsl setLightness]]))


(defn darken [color amount]
  (let [lightness ((js->clj (parseToHsl color)) "lightness")]
    (setLightness (max 0 (- lightness (/ amount 100))) color)))

(defn lighten [color amount]
  (let [lightness ((js->clj (parseToHsl color)) "lightness")]
    (setLightness (min 1 (+ lightness (/ amount 100))) color)))


(def global-styles
  {"*, *:before, *:after"
   {:margin 0
    :padding 0
    :border 0
    :outline 0
    :color :inherit
    :textDecoration :none
    :boxSizing :border-box
    :list-style :none
    :fontSize "16px"
    :fontFamily "\"Source Sans Pro\", sans-serif"
    :fontWeight "400"
    :background :none
    :overflow :visible
    :borderRadius 0}

   "*:focus"
   {:outline 0}

   "*:visited"
   {:color :inherit}

   "html, body, #app"
   {:height "100%"
    :overflow :hidden
    :WebkitFontSmoothing :antialiased
    :MozOsxFontSmoothing :grayscale
    :lineHeight "1"
    :WebkitTextSizeAdjust "100%"}


   "button::-moz-focus-inner,
    [type=\"button\"]::-moz-focus-inner,
    [type=\"reset\"]::-moz-focus-inner,
    [type=\"submit\"]::-moz-focus-inner"
   {:border :none
    :padding :none}

   "button::-moz-focusring,
    [type=\"button\"]::-moz-focusring,
    [type=\"reset\"]::-moz-focusring,
    [type=\"submit\"]::-moz-focusring"
   {:outline :none}

   "button,
    [type=\"button\"],
    [type=\"reset\"],
    [type=\"submit\"]"
   {:WebkitAppearance :button}

   "[type=\"number\"]::-webkit-inner-spin-button,
    [type=\"number\"]::-webkit-outer-spin-button"
   {:height :auto}

   "[type=\"search\"]"
   {:WebkitAppearance :textfield}

   "[type=\"search\"]::-webkit-search-decoration"
   {:WebkitAppearance :none}

   "::-webkit-file-upload-button"
   {:WebkitAppearance :button
    :font :inherit}

   "main"
   {:display :block}

   "input[hidden]"
   {:display :none}})