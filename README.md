# AstrBot Android APK

This repository contains an Android WebView client for an existing AstrBot server. It does not bundle the AstrBot backend, plugins, model, or chat history.

## Build

GitHub Actions builds a debug APK when the workflow is added/updated on the default branch. Open the Actions tab, open the latest "Build AstrBot Android APK" run, and download the `AstrBot-Mobile-APK` artifact. Extract it to get `app-debug.apk`.

## Connect

On first launch, enter the URL of the AstrBot server that the phone can reach, for example `http://192.168.1.23:6185`. Do not use `127.0.0.1` unless AstrBot itself is running on that phone. The phone and server usually need to share a network, or the server needs a secure remotely reachable address.

The app accepts HTTP for local network deployments. Do not expose an unencrypted admin dashboard to the public internet.
