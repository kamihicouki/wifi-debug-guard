# アップロード用AABの署名

通常のCIは署名鍵を持たず、未署名のrelease成果物を検証します。Playへのアップロード用は自分の端末上で署名します。

```sh
# JDK 17のJAVA_HOMEとAndroid SDKのANDROID_HOMEを設定して実行
python3 scripts/build-upload-bundle.py --create-upload-key
# 以後は既存鍵を使う
python3 scripts/build-upload-bundle.py
```

- 生成AAB: `app/build/outputs/bundle/release/app-release.aab`
- ローカル鍵と資格情報: Git対象外の `signing/`（ディレクトリ0700、ファイル0600）。秘密のまま安全にバックアップする。
- 元端末専用版の鍵とは別物。Google Playへのアップロード、Play App Signingへの同意、配信署名の確定はまだ行っていない。
- `WDG_UPLOAD_STORE`、`WDG_UPLOAD_ALIAS`、`WDG_UPLOAD_PASSWORD` をGradleへ渡す構成。ログ・CI変数・Issueに秘密値を記載しない。
- テスト後に通常の未署名ビルドを実行すると出力先は上書きされる。提出直前に署名ビルドを実行し、`jarsigner -verify` とSHA-256を再確認する。

開発者登録が有効になるまではAABを提出できない。鍵作成だけでは審査・公開は進まない。
