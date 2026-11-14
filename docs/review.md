# 公開後レビューと対応

2026-11-14、インストール済みGoogle Antigravityのモデル選択UIで **Gemini 4 Carbon Medium** を確認・選択し、公開コミット `29ea9e8` の読み取り専用レビューを実施。レビュー中にCI修正コミット `726f974` が追加されたが、アプリ本体は同一。レビュー担当は静的解析のみで、実機操作・テスト再実行・編集は実施していない。以下はCodex側で検証し、1.1.1で対応した結果。

| レビュー指摘 | 判定・対応 |
| --- | --- |
| 正確な位置情報・バックグラウンド位置情報の未確認 | 採用。開始時に拒否、サービス評価でも未付与時はOFF要求と専用状態表示。 |
| 設定書き込み直後のON表示・拒否時の再要求 | 一部採用。表示を「設定値ON（ADB接続未確認）」に変更。OS側の非同期承認・拒否とADB接続成功は保証しない。再試行は自動復帰のため継続し、READMEへ明記。ContentObserverと再試行抑止は未実装。 |
| 停止競合でサービスが残る・停止警告を上書き | 採用。停止済みなら評価せず自己停止し、状態を保持。停止時NOT_STICKY、通常管理中STICKYを維持。 |
| 例外時に古いON通知が残る | 採用。エラーも同じ状態・通知更新処理を使う。 |
| 引用符を含むSSIDの二重正規化 | 再現して採用。保存済みSSIDを再正規化しない。引用符だけのSSIDも含め20ケースで検証。 |
| USER_UNLOCKEDの静的登録とFGS開始例外 | 採用。不要なactionを削除し、boot/update actionだけを許可。起動失敗を状態に記録。Activityからの開始失敗も捕捉。 |
| 動的レシーバへ任意アプリがbroadcastを連打可能 | Android 13対象では不採用。登録している3 actionはすべてAOSPのprotected-broadcastであり一般アプリから送信できない。将来target SDK変更時は再確認。 |
| CIが旧toolsパッケージ取得で失敗 | レビュー完了前に修正済み。platform-toolsを指定し、GitHub Actions成功。 |
| 通知・バックアップ・BSSID・poll・UI・旧生成物 | 通知権限を開始条件に追加、標準バックアップ/端末移行の除外XMLを追加、バンド切替とスリープ時の遅延を説明、最終確認時刻を表示、Activity破棄時にdialogを閉じる。検証クラスは一時フォルダーへ出力。旧生成物はGit追跡外。 |

## 発見したドキュメントとコードの差

- 「常駐通知付き」でも通知権限未付与の開始を許容していた。権限確認を追加し、チャンネル設定による非表示は説明を補足。
- 「バックアップ無効」はallowBackupだけではメーカー独自の端末移行まで保証できない。標準移行の除外を追加し、説明を保証できる範囲に修正。
- 「15秒間隔」はCPUスリープ中の実時間を保証しない。CPU稼働中の間隔であることを明記。
- 単一ルーターの周波数帯切替でもBSSIDが変わる点が未記載。対象が1 BSSIDである実装に合わせて補足。
- 設定値ONは実際のADB接続・OSによる最終承認を示さない。画面表記と説明を限定。

POST_NOTIFICATIONSのADBコマンドがAndroid 13以上向けであることは元のREADMEに既に記載されており、この点は矛盾として採用していない。

根拠: [Android 13 protected broadcasts](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/core/res/AndroidManifest.xml)、[Android Auto Backup](https://developer.android.com/identity/data/autobackup)、[Android 13 ADB implementation](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/services/core/java/com/android/server/adb/AdbDebuggingManager.java)。

## 残る制約

公開版でのON/OFF・ロック解除前起動・再起動の一連の実機受入は未実施。SSID/BSSIDの偽装、OSによる権限失効、強制停止、メーカー制限、CPUスリープ中の遅延は残る。判定・ビルド・Lint・初期起動の検証はこれらの動的動作を保証しない。
