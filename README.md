# Wi-Fi Debug Guard

指定したWi-Fiアクセスポイントへの接続中だけ、Androidのワイヤレスデバッグを自動で有効化する小さなAndroidアプリです。切断・別のWi-Fi・接続先不明・位置情報OFFでは無効化を要求します。初期状態は停止中です。**Android 13向けの実験的な、ADB利用者のためのツール**です。

## 動作

- アプリで「現在接続中のWi-Fiを登録」を選び、SSIDとBSSIDを確認して保存します。対象は1アクセスポイントです。
- 「自動管理を開始」でAndroid 13以上では通知権限も確認して常駐通知付きサービスを起動します。通知チャンネルを無効にすると通知欄に表示されない場合があります。SSIDとBSSIDの両方が一致するときだけONを要求します。
- Wi-Fiの状態変化・位置情報設定の変更・ネットワークコールバックとCPU稼働中の15秒間隔の確認（スリープ中は遅れます）を使います。即時の遮断を保証するファイアウォールではありません。
- 管理を有効にした場合、再起動・アプリ更新後に自動再開を試みます。強制停止、権限、メーカーの省電力制限で動作しない場合があります。
- 登録し直すと自動管理を停止し、OFFを要求します。確認後に開始してください。
- 「自動管理を停止してデバッグOFF」で管理を停止します。OFFに失敗した場合は端末の開発者向けオプションでOFFにしてください。

## 必要なものと制約

Android 11以上でインストールできますが、主な対象はAndroid 13（targetSdk 33）です。他のAndroidバージョン、メーカー独自OS、PIN設定後の初回ロック解除前の動作は未検証です。Google Play配布を目的とした構成ではありません。

`Settings.Global` の `adb_wifi_enabled` は通常のアプリ向けに保証された制御APIではありません。`WRITE_SECURE_SETTINGS` のADBによる明示付与が必要で、OS更新やメーカーにより動作しないことがあります。権限がなければ本アプリはONにもOFFにもできません。

Android側のネットワーク承認、デバッグ許可、PCとのペアリングは別途必要です。アプリは承認ダイアログを迂回しません。未承認の場合も評価ごとに有効化を再要求するため、先に端末側の承認を完了してください。表示は設定値であり、OSの非同期の拒否を即時に反映できるとは限りません。信頼できる私有ネットワークでのみ使用し、最初に端末設定のワイヤレスデバッグから対象ネットワークを許可してください。

SSID/BSSIDは偽装可能で、ネットワークの安全性を認証するものではありません。ADBで承認したPCには強い端末操作権限があります。共有Wi-Fiでは利用を避け、不要なPCのペアリングは解除してください。同じルーターの周波数帯切替、ルーター交換、メッシュAP切替、ホットスポットの再生成でBSSIDが変わった場合は再登録が必要です。

## ビルド

JDK 17、Android SDK Platform 34、Android SDK Build-Tools、ネットワーク接続が必要です。Android Studioで開くか、`ANDROID_HOME` を自分のSDKに設定してください。Gradle WrapperはGradle 8.7、Android Gradle Pluginは8.5.2です。

```sh
./verify.sh
# debug APK（開発用のローカルdebug鍵で署名）
adb install app/build/outputs/apk/debug/app-debug.apk
```

`verify.sh` はWi-Fi判定の回帰テスト、debug/releaseビルド、Android Lintを実行します。release APKは未署名です。配布・継続更新する場合は自分の秘密鍵で署名し、その鍵を安全に保管してください。署名鍵・SDKパス・APKはGitに追加しないでください。依存関係は各提供元から取得し、そのライセンスに従います。

公開版のapplication IDは `io.github.kamihicouki.wifidebugguard` です。端末専用の旧版とは別アプリです。**同じ端末でデバッグ設定を管理するアプリを複数同時に有効にしないでください。**

## 初回セットアップ

1. 開発者向けオプションを有効にし、信頼できるPCからUSB ADBで接続します。複数端末を接続している場合は以下の各 `adb` に `-s <接続先>` を指定してください。
2. ビルドしたAPKをインストールし、一度アプリを開きます。
3. 以下の権限を明示的に付与します。位置情報はWi-FiのSSID/BSSIDをバックグラウンドで識別するために使います。GPS座標は取得しません。

```sh
adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.WRITE_SECURE_SETTINGS
adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.ACCESS_FINE_LOCATION
adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.ACCESS_BACKGROUND_LOCATION
adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.POST_NOTIFICATIONS
```

通知権限のコマンドはAndroid 13以上向けです。端末の位置情報設定をONにし、アプリの位置情報権限が「常に許可」「正確な位置情報」になっていることを確認してください。省電力制限によって停止する場合は、端末のアプリ設定からバックグラウンド実行を許可してください。

4. 対象Wi-Fiに接続し、端末のワイヤレスデバッグ画面で対象ネットワークを承認します。必要に応じてPCを `adb pair <IP:ペアリングポート>` でペアリングします。ペアリングポートと接続ポートは別です。
5. アプリでWi-Fiを登録し、表示を確認して自動管理を開始します。
6. 通知・画面は最終評価時点の状態であり、設定値ONはPCとの接続成功を保証しません。端末設定でONを確認し、別Wi-Fi・切断・位置情報OFFでOFFになることを自分の端末で確認してください。接続ポートは変わるため、PC側で `adb mdns services` または端末画面を確認し、`adb connect <IP:接続ポート>` で再接続します。

OFF後はワイヤレスADBが切れるため、再開や権限の復旧にUSB接続が必要な場合があります。アンインストール前は管理を停止し、端末の設定でワイヤレスデバッグがOFFであることを確認してください。アプリを強制停止・削除するだけではOFFを保証しません。

## プライバシー

インターネット権限、広告、解析SDK、外部送信機能はありません。選択したSSID/BSSID、管理状態、状態メッセージ、最終確認時刻を端末のアプリ専用ストレージに保存します。再起動直後にも使うため端末保護ストレージを使用し、クラウドバックアップと標準の端末移行から除外する設定です（独自の端末移行機能については保証しません）。SSID/BSSID、パスワード、ペアリングコードをログに出力しません。問題報告に端末ログやスクリーンショットを添付するときは、Wi-Fi識別子、IP、端末番号、ペアリング情報を削除してください。

## 検証状況

公開版1.1.1では20ケースの判定テスト、debug/releaseビルド、Lintを実施しました。Android 13実機で別パッケージとして新規インストール・起動し、未登録ではサービスが起動せず、既存アプリと現在のデバッグ設定を変更しないことを確認後、テスト用アプリを削除しました。

旧端末専用版ではWi-Fi離脱・再接続、位置情報切替、再起動後の復帰が確認されていますが、**公開版の登録UIを含むON/OFF・再起動の一連の実機受入試験は未実施**です。既存の稼働環境を維持するため公開版による設定切替を行っていません。全端末での動作を保証するものではありません。

Carbonによる公開後レビューと対応は [レビュー記録](docs/review.md) を参照してください。

実装の根拠: [Android 13 AdbDebuggingManager](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/services/core/java/com/android/server/adb/AdbDebuggingManager.java)、[ADBの公式説明](https://developer.android.com/tools/adb)、[Wi-Fi権限](https://developer.android.com/develop/connectivity/wifi/wifi-permissions)。

## ライセンス

[MIT License](LICENSE)。Gradle Wrapperなどの第三者ファイルについては [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) を参照してください。
