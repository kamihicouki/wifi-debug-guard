# Wi-Fi Debug Guard

指定したWi-Fiアクセスポイントへの接続中だけ、Androidのワイヤレスデバッグを自動で有効化する小さなAndroidアプリです。切断・別のWi-Fi・接続先不明・位置情報OFFでは無効化を要求します。初期状態は停止中です。**ADB利用者のための実験的なツール**です。

## 動作

画面は「自動管理」「ワイヤレスデバッグの現在の設定」「Wi-Fiの判定」「次に必要な操作」を分けて表示します。

- 「自動管理」は、管理の開始要求と、このアプリ自身のサービスが現在動いているかを分けて「管理中／開始待ち／停止処理中／停止中」と表示します。
- デバッグのカードはAndroidの現在の設定値を読み、「ON／OFF／確認不能」と表示します。自動管理が停止していても、デバッグが手動でONになっていればONです。PCとのADB接続成功を示すものではありません。
- Wi-Fiのカードは現在の接続先と登録したSSID/BSSIDの一致を表示します。管理の開始・停止やデバッグの設定値とは別の判定です。
- 「次に必要な操作」から、利用説明、権限、ADB設定、位置情報ON、Wi-Fi登録などの不足を確認できます。最終判定の時刻とメッセージも表示します。

### 開始・停止と端末起動時の自動開始

| 操作・イベント | 現在の管理 | 次の端末起動時 |
| --- | --- | --- |
| 「自動管理を開始」 | 必要な同意・権限・位置情報ON・対象Wi-Fi登録を確認して開始を要求 | 自動開始もONになる |
| 「管理を停止してデバッグをOFF」→確認ダイアログで停止 | 管理を停止し、デバッグOFFを要求。無線ADBは切断される場合がある | 自動開始もOFFになる |
| 「端末起動時に自動で開始」をON／OFF | 現在の管理とデバッグ設定は変更しない | 次の起動の設定だけを変更する |
| 端末の起動 | 自動開始がONで、同意・対象Wi-Fi登録・各権限・位置情報ONが揃っていれば開始を要求 | 現在の管理が停止中でも自動開始ONなら対象になる |
| アプリ更新 | 現在の管理が有効な場合だけ再開を要求。自動開始スイッチだけがONでも開始しない | 設定を維持する |

初期状態は現在の管理も起動時の自動開始もOFFです。以前の版から更新し、自動開始の設定がまだ保存されていない場合は、以前の管理の有効・無効を初期値として引き継ぎます。停止後に次の端末起動時だけ開始したい場合は、停止してから自動開始スイッチだけをONにします。

起動時の条件が足りない場合は自動起動を待機し、状態メッセージに不足を記録します。API 36対応のlocation型サービスのため、位置情報OFFで再起動した場合は、位置情報をONにした後にアプリから再開してください。スイッチONや開始要求の保存だけでは、サービス起動成功を意味しません。強制停止、権限、メーカーの省電力制限などでも動作しない場合があります。

### Shizukuの停止通知

自動管理中にShizukuサービスのBinderが稼働していることを確認し、そのBinderが停止すると、端末に「Shizukuが停止しました」を1回通知します。次に稼働を確認すると、次の停止を通知できる状態になります。監視開始時にBinderを受け取れない場合は未確認です。監視はネットワーク接続に依存しません。

Androidでアプリの通知と「Shizukuの停止」通知チャンネルを許可してください。監視にShizukuの操作権限は不要です。ADBの設定変更は既存のWi-Fi管理が行います。

### 対象Wi-Fiと評価

「接続中のWi-Fiを対象として登録」でSSIDとBSSIDを確認して保存します。対象は1アクセスポイントです。登録し直しても管理を停止しません。管理中なら新しい対象で再評価し、停止中なら登録だけを行い、デバッグ設定を変更しません。

SSIDとBSSIDの両方が一致するときだけONを要求します。切断・別のWi-Fi・接続先不明・位置情報OFFでは、稼働中の管理サービスがOFFを要求します。Wi-Fiの状態変化、位置情報設定の変更、ネットワークコールバックとCPU稼働中の15秒間隔の確認を使います。スリープ中は遅れます。即時遮断を保証するファイアウォールではありません。

停止時や評価時にOFFを要求できない場合は、端末の開発者向けオプションでOFFにしてください。停止ボタンの確認だけでOFF成功を保証しません。

## 必要なものと制約

Android 11以上でインストールできますが、ビルドはcompile/target SDK 36です。Android 13で初期起動を確認しています。他のAndroidバージョン、メーカー独自OS、PIN設定後の初回ロック解除前の動作は未検証です。Google Playへの提出準備中で、審査・ストア公開は未完了です。

`Settings.Global` の `adb_wifi_enabled` は通常のアプリ向けに保証された制御APIではありません。`WRITE_SECURE_SETTINGS` のADBによる明示付与が必要で、OS更新やメーカーにより動作しないことがあります。権限がなければ本アプリはONにもOFFにもできません。

Android側のネットワーク承認、デバッグ許可、PCとのペアリングは別途必要です。アプリは承認ダイアログを迂回しません。未承認の場合も評価ごとに有効化を再要求するため、先に端末側の承認を完了してください。デバッグのカードはAndroidの設定値を継続して読み取りますが、OSの非同期の拒否や変更を遅れなく反映できるとは限りません。最終判定メッセージは評価時点の記録です。信頼できる私有ネットワークでのみ使用し、最初に端末設定のワイヤレスデバッグから対象ネットワークを許可してください。

SSID/BSSIDは偽装可能で、ネットワークの安全性を認証するものではありません。ADBで承認したPCには強い端末操作権限があります。共有Wi-Fiでは利用を避け、不要なPCのペアリングは解除してください。同じルーターの周波数帯切替、ルーター交換、メッシュAP切替、ホットスポットの再生成でBSSIDが変わった場合は再登録が必要です。

## ビルド

JDK 17、Android SDK Platform 36、Android SDK Build-Tools、ネットワーク接続が必要です。Android Studioで開くか、`ANDROID_HOME` を自分のSDKに設定してください。Gradle WrapperはGradle 8.13、Android Gradle Pluginは8.13.2です。

```sh
./verify.sh
# debug APK（開発用のローカルdebug鍵で署名）
adb install app/build/outputs/apk/debug/app-debug.apk
```

`verify.sh` はWi-Fi判定20件・セットアップ順序7件・起動判定18件・画面状態13件・Shizukuの状態遷移12件（計70件）のテスト、debug/release APK・AABビルド、Android Lintを実行します。通常のreleaseビルドは未署名です。配布・継続更新する場合は自分の秘密鍵で署名し、その鍵を安全に保管してください。署名鍵・SDKパス・APKはGitに追加しないでください。依存関係は各提供元から取得し、そのライセンスに従います。

通常のapplication IDは `io.github.kamihicouki.wifidebugguard` です。端末専用の旧版とは別アプリです。**同じ端末でデバッグ設定を管理するアプリを複数同時に有効にしないでください。**

既存の端末専用版を置き換えるためのビルドでは、`-PguardApplicationId=jp.solutus.wifidebugguard` でapplication IDを指定できます。たとえば `./gradlew -PguardApplicationId=jp.solutus.wifidebugguard :app:assembleRelease` です。既存版の更新には同じ署名鍵が必要で、このオプションだけで更新できるわけではありません。元の鍵や設定を公開・置換せず、バックアップとUSB ADB復旧経路を準備してから対象端末だけで更新します。公開用の通常ビルドへ端末専用IDを混ぜないでください。

## 初回セットアップ

1. 開発者向けオプションを有効にし、信頼できるPCからUSB ADBで接続します。複数端末を接続している場合は以下の各 `adb` に `-s <接続先>` を指定してください。
2. ビルドしたAPKをインストールし、一度アプリを開きます。
3. アプリの「権限・初回設定を確認」から、位置情報の利用説明を読み、同意した場合だけAndroidの権限画面へ進んでください。位置情報は「正確」「常に許可」、Android 13以上は通知を許可します。強い設定権限はPCで以下の最初のADBコマンドを実行します。残りのコマンドは必要に応じた手動設定用です。位置情報はWi-FiのSSID/BSSIDをバックグラウンドで識別するために使います。GPS座標は取得しません。

```sh
adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.WRITE_SECURE_SETTINGS
adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.ACCESS_FINE_LOCATION
adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.ACCESS_BACKGROUND_LOCATION
adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.POST_NOTIFICATIONS
```

ADBで位置情報権限を許可してもアプリ内の開示への同意は省略できません。通知権限のコマンドはAndroid 13以上向けです。端末の位置情報設定をONにし、アプリの位置情報権限が「常に許可」「正確な位置情報」になっていることを確認してください。省電力制限によって停止する場合は、端末のアプリ設定からバックグラウンド実行を許可してください。

4. 対象Wi-Fiに接続し、端末のワイヤレスデバッグ画面で対象ネットワークを承認します。必要に応じてPCを `adb pair <IP:ペアリングポート>` でペアリングします。ペアリングポートと接続ポートは別です。
5. アプリでWi-Fiを登録し、表示を確認して自動管理を開始します。
6. 画面のデバッグカードは現在のAndroid設定値、通知・最終判定は評価時点の状態です。設定値ONはPCとの接続成功を保証しません。端末設定でONを確認し、別Wi-Fi・切断・位置情報OFFでOFFになることを自分の端末で確認してください。接続ポートは変わるため、PC側で `adb mdns services` または端末画面を確認し、`adb connect <IP:接続ポート>` で再接続します。

OFF後はワイヤレスADBが切れるため、再開や権限の復旧にUSB接続が必要な場合があります。アンインストール前は管理を停止し、端末の設定でワイヤレスデバッグがOFFであることを確認してください。アプリを強制停止・削除するだけではOFFを保証しません。

## Google Playへの提出準備

現時点ではGoogle Playへ未提出・未公開です。[提出チェックリスト](docs/play/release-checklist.md)と[掲載・権限申告案](docs/play/README.md)を参照してください。審査承認を保証しません。位置情報の審査動画、通し動作の受入、有効な開発者登録が必要です。

アップロード鍵を自分で作成する場合は、JDK17のJAVA_HOMEを設定して以下を実行します。鍵・パスワードはGit対象外の`signing/`へ保存し、暗号化した安全な場所へ別途バックアップしてください。元端末版の署名鍵を流用しません。既存鍵がある場合は`--create-upload-key`を外します。

```sh
python3 scripts/build-upload-bundle.py --create-upload-key
```

生成先は `app/build/outputs/bundle/release/app-release.aab` です。アップロード鍵の作成と、Google Play App Signingの契約・実際の配信署名は別です。

## プライバシー

インターネット権限、広告、解析SDK、外部送信機能はありません。選択したSSID/BSSID、現在の管理設定、起動時の自動開始設定、利用説明への同意、状態メッセージ、最終確認時刻、自動起動を要求した時刻と起動イベントの種類を端末のアプリ専用ストレージに保存します。画面表示中も、同意と必要な権限・位置情報ONが揃えば接続先を読み取り、管理の開始・停止とは別に表示します。再起動直後にも使うため端末保護ストレージを使用し、クラウドバックアップと標準の端末移行から除外する設定です（独自の端末移行機能については保証しません）。SSID/BSSID、パスワード、ペアリングコードをログに出力しません。[プライバシーポリシー](docs/play/privacy-policy.md)をアプリ内から開けます。問題報告に端末ログやスクリーンショットを添付するときは、Wi-Fi識別子、IP、端末番号、ペアリング情報を削除してください。

## 検証状況

1.3.0（versionCode 6）の変更では、Wi-Fi判定20件・セットアップ順序7件・起動判定18件・画面状態13件の計58件、debug/release APK・AABビルド、Lintの成功を確認しています。これらは起動条件や表示の判定を確認するもので、端末でサービスが確実に動くことを保証しません。

以前の1.2.0ではAndroid 13での初期起動と、未登録のまま既存デバッグ設定を変更しないことを確認しました。**1.3.0の登録・ON/OFF・Wi-Fi再接続・再起動後の自動復帰とMac再接続の通し実機結果は、まだ確定していません。** Android 16上の動作、メーカー独自OS、PIN設定後の初回ロック解除前の動作も未検証です。既存端末版の過去の成功を、この版の成功として扱いません。

今回の端末への導入・更新と実機検証は、Google Play公開完了を待たずに進める方針です。これはGoogle Playへの提出・審査・公開が完了したという意味ではありません。運用端末を変更する前に、元の署名・データと復旧経路を確保し、他の作業や同時管理と衝突しないことを確認します。

Carbonによる公開後レビューと対応は [レビュー記録](docs/review.md) を参照してください。

実装の根拠: [Android 13 AdbDebuggingManager](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/services/core/java/com/android/server/adb/AdbDebuggingManager.java)、[ADBの公式説明](https://developer.android.com/tools/adb)、[Wi-Fi権限](https://developer.android.com/develop/connectivity/wifi/wifi-permissions)。

## ライセンス

[MIT License](LICENSE)。Gradle Wrapperなどの第三者ファイルについては [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) を参照してください。
