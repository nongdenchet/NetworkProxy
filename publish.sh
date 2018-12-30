#!/usr/bin/env bash

# Publish no-op
./gradlew :networkproxy-no-op:clean :networkproxy-no-op:build :networkproxy-no-op:install :networkproxy-no-op:bintrayUpload -Ppublish=true

# Publish lib
./gradlew :networkproxy:clean :networkproxy:build :networkproxy:install :networkproxy:bintrayUpload -Ppublish=true
