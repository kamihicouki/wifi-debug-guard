# Play Console申告案と審査用デモ

2026-11-14時点の草稿。Consoleには未入力・未提出です。最終AAB・依存SDK・画面・全トラックを確認して申告を確定します。

## Data safety

現在の実装を前提に「収集なし・共有なし」を申告する案です。Wi-Fi識別子を取得しないという意味ではありません。SSID/BSSIDを位置情報権限で取得し、端末内で処理・保存します。GoogleのData safetyでは、端末外へ送らないローカル処理は収集の申告範囲外です。権限審査の省略にはなりません。[公式Data safetyガイド](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en)

| 項目 | 申告案・根拠 |
| --- | --- |
| ユーザーデータの収集・共有 | なし。INTERNET権限、ネットワーク送信、広告・解析SDKなし。最終merged manifestと依存一覧を再検査する。 |
| 端末内データ | 登録SSID/BSSID、管理設定、状態、確認時刻、導入設定。開発者へ送らない。 |
| アカウント作成 | アプリのユーザーアカウントなし。ログインなし。 |
| 削除 | Androidのアプリデータ消去／アンインストールでローカル設定を削除。先に管理停止と端末設定OFFを確認する。 |
| 転送時暗号化 | 送信機能がないため「暗号化して送信する」と広告しない。Consoleで表示される質問に沿って回答する。 |
| 第三者SDK・外部ページ | 現在のアプリには送信SDKなし。外部サポートページを開く実装を追加した場合は、その導線を再確認。 |

## バックグラウンド位置情報：申告対象は1機能

主な目的: ユーザーが選択したWi-FiアクセスポイントでのみワイヤレスデバッグONを要求し、それ以外でOFFを要求する自動管理。

背景利用の説明案:

> Wi-Fi Debug Guard reads the connected Wi-Fi SSID and BSSID through Android location permission, including when its screen is closed, to compare the current access point with the one selected by the user. This comparison is the app's core wireless-debugging management feature. Without background access, leaving the app screen prevents reliable network identification and the app cannot perform the selected management. No GPS coordinates or movement history are requested, and no Wi-Fi identifiers are sent off device. Management is stopped by default and starts only after user setup.

利便性だけの利用と判断される可能性があり、この説明で承認されると保証しません。機能のユーザー利益・背景利用の必要性は審査対象です。位置情報権限が便利だからという理由で要求を増やさず、不要なら削除します。機能維持に必要か最終実機テストで評価します。[公式background locationガイド](https://support.google.com/googleplay/android-developer/answer/9799150?hl=en)

目立つアプリ内開示の文案（通常の権限説明やREADMEだけで代用しない）:

> Wi-Fi Debug Guardは、アプリを閉じている間やバックグラウンドでも、指定Wi-Fiに応じてワイヤレスデバッグを切り替えるため、位置情報の権限でWi-Fi名（SSID）とアクセスポイント識別子（BSSID）を読み取ります。GPS座標や移動履歴は取得せず、Wi-Fi情報は端末内だけで使用し、外部に送信しません。

この開示を通常の導入操作中に表示し、その後に正確な位置情報のruntime prompt、Androidの制約に沿った「常に許可」設定への導線を示します。ADBによる許可だけを動画に映してruntime promptを省略しません。実装済みかと文面一致は提出直前に確認します。

## WRITE_SECURE_SETTINGSとforeground service

`WRITE_SECURE_SETTINGS` はユーザーが信頼するPCのADBから明示付与する開発用権限です。アプリの主目的に必要ですが、一般アプリ向けに保証されたデバッグ制御APIではありません。確認した公式ポリシーにこの権限を名指しした一律禁止の記述は見つからず、承認可能とも断定しません。最終アップロードでの自動検査とGoogleの審査結果を確認します。権限名を隠す、別機能に偽装する、ADB制約を掲載文で省略することはしません。

FGSは実装が選択した正確なtypeと利用目的をConsoleにも申告します。位置情報アクセスを行うならlocationとして説明し、審査回避のためspecialUse等へ偽装しません。起動、継続、停止、再起動後の挙動を実機で示します。[公式FGS案内](https://developer.android.com/develop/background-work/services/fgs/service-types)

## App access：再現手順案

ログイン・有料壁はありません。ただし、審査担当者もADB設定なしでは主機能を再現できません。以下をアクセス手順へ明示し、審査担当者が実行できるかは未確認です。

1. Androidの開発者向けオプションを有効にして、信頼できるテストPCをUSB ADBで承認する。
2. `adb shell pm grant io.github.kamihicouki.wifidebugguard android.permission.WRITE_SECURE_SETTINGS` を実行する。端末の複数接続時は明示的な接続先を指定する。
3. アプリの開示・runtime promptから正確な位置情報、背景位置情報、必要な通知を設定する。位置情報のシステム設定をONにする。
4. 専用のテストAPでAndroidのワイヤレスデバッグネットワーク承認とPCペアリングを行う。審査用に個人のWi-Fi・鍵・コードを配布しない。
5. アプリで現在Wi-Fiを登録し管理開始。背景に移してON要求、別AP／切断でOFF要求、管理停止と手動復旧を確認する。
6. USB復旧経路を確保してから再起動後の自動復帰・PC再接続を検証する。

## 証拠動画：未撮影

推奨30秒以下の実Android画面動画を用意し、誰でも審査時に閲覧できる限定公開URLを申告します。実動作を示す動画でありモックは使いません。動画には背景動作、目立つ開示、runtime promptを含めます。

| 目安 | 実画面 |
| --- | --- |
| 0–7秒 | 新規状態の開始導線→位置情報の目立つ開示→正確な位置情報のruntime prompt。 |
| 7–14秒 | 背景位置情報の説明とAndroid「常に許可」設定。 |
| 14–22秒 | 個人情報を含まないテストAPを登録して管理開始、背景に移して通知／端末設定のONを確認。 |
| 22–30秒 | 対象外AP／切断へ切替、OFFと停止を確認。実際の待機を切り詰める場合は編集箇所を明示し、即時性を偽らない。 |

ADB導入手順は別の短い実演・字幕に分け、権限付与が必要な事実を隠さない。SSID/BSSID、IP、端末番号、Googleアカウント、通知、ペアリングコードを撮影前にテスト用にする。実機通し未検証のまま動画完了と記載しない。
