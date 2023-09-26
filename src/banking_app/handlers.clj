(ns banking-app.handlers
  (:require [yada.yada :refer [as-resource]]))

;; TODO: Create handlers for basic CRUD
(def create-account (as-resource "created"))
(def view-account (as-resource "100"))
(def deposit (as-resource "200"))
(def withdraw (as-resource "300"))
