# Wi-Fi Debug Guard プライバシーポリシー

作成日: 2026年11月14日。対象: Wi-Fi Debug Guard（アプリID `io.github.kamihicouki.wifidebugguard`）。開発者: kamihicouki。

## 端末内で使用する情報

Wi-Fi Debug Guardは、指定されたWi-Fiでワイヤレスデバッグを自動管理するため、位置情報の権限を使用して、現在接続中のWi-Fi名（SSID）とアクセスポイント識別子（BSSID）を読み取ります。管理を有効にしている間は、アプリ画面を閉じているときやバックグラウンドでもこの情報を使用します。位置情報のシステム設定や必要な権限が使えない場合、デバッグOFFを要求します。

GPS座標、移動経路、Wi-Fiパスワード、ADBのペアリングコードは取得・保存しません。Androidによるネットワーク承認とPCとのペアリングは、Android自身の機能です。

登録したSSID/BSSID、管理の有効・無効、状態メッセージ、最終評価時刻、権限説明への応答など、設定に必要な情報をアプリ専用の端末内ストレージに保存します。再起動後に動作するため、登録設定には端末保護ストレージを使用します。標準のクラウドバックアップと端末移行から除外する設定ですが、メーカー独自の移行機能まで保証するものではありません。

## 外部送信・共有

アプリにはインターネット権限、広告、解析SDK、外部サーバーへの送信機能はありません。アプリから開発者がWi-Fi識別子や位置情報を収集することはなく、第三者へ販売・共有しません。状態診断用ログにはSSID/BSSID、Wi-Fiパスワード、ペアリングコードを出力しません。

端末のデバッグをONにすると、ユーザーがAndroid上で承認したPCがADBで端末を操作できるようになります。このアクセスはAndroidのデバッグ機能によるもので、アプリの開発者へデータを送信するものではありません。信頼できるPCと私有ネットワークでのみ使用し、不要なPCの承認を解除してください。

## 保持期間・削除・権限の管理

設定は、ユーザーが変更するか、Androidのアプリ情報からアプリデータを消去するか、アンインストールするまで端末に残ります。開発者が保管するユーザーアカウントやサーバー上のユーザーデータはありません。

管理の停止はアプリ内で行えます。削除や権限取消しの前に管理を停止し、端末の開発者向けオプションでワイヤレスデバッグOFFを確認してください。アプリデータ消去、強制停止、アンインストールだけではデバッグOFFを保証しません。権限はAndroidのアプリ設定で取り消せますが、取り消すと自動管理できなくなる場合があります。

## お問い合わせと外部ページ

プライバシーに関するお問い合わせは [公開GitHub Issues](https://github.com/kamihicouki/wifi-debug-guard/issues) を利用できます。Issuesは公開されます。Wi-Fi識別子、IP、端末番号、鍵、ペアリングコード、個人情報を投稿しないでください。外部ページを開いた場合、そのサービスの規約・プライバシーポリシーが適用されます。

## 変更

実装や情報の取り扱いが変わる場合は、このポリシーの日付と内容を更新します。

---

# Wi-Fi Debug Guard Privacy Policy

Prepared: November 14, 2026. App: Wi-Fi Debug Guard (`io.github.kamihicouki.wifidebugguard`). Developer: kamihicouki.

Wi-Fi Debug Guard uses location permission to read the connected Wi-Fi SSID and BSSID to manage wireless debugging for one selected access point. When management is enabled, it also uses this information in the background or when the app screen is closed. It does not request GPS coordinates, track movement, or store Wi-Fi passwords or ADB pairing codes.

The selected network, enabled state, status, check time and setup choices are stored in the app's local storage. Device-protected storage supports startup after reboot. Standard cloud backup and device transfer are excluded; manufacturer-specific migration cannot be guaranteed.

The app has no Internet permission, advertising, analytics SDK or server upload. The developer does not receive your Wi-Fi identifiers or location from the app, or sell or share them. Diagnostic status logs omit SSID/BSSID, passwords and pairing codes. Enabling Android debugging lets computers approved by you use ADB; it does not send information to the app developer.

Local settings remain until changed, cleared through Android app settings, or removed with the app. There is no developer-operated user account or server-side user dataset. Stop management and confirm debugging is OFF in Android settings before clearing data or uninstalling. Removing or force-stopping the app alone does not guarantee debugging is OFF. Permissions can be revoked in Android settings and may be required for the feature to work.

Privacy questions: [public GitHub Issues](https://github.com/kamihicouki/wifi-debug-guard/issues). Do not post personal information, network identifiers, IP addresses, device serials, keys or pairing codes. External services have their own policies. This policy will be updated when the app's data handling changes.
