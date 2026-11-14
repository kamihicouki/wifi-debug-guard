# Changelog

## 1.2.0

- Prepare API 36 builds and location foreground-service declarations for Google Play submission.
- Add prominent location disclosure and incremental runtime-permission setup.
- Require disclosure consent before Wi-Fi access and boot restart.
- Add original app/store artwork, privacy policy, listing and permissions-declaration drafts.
- Add private upload-key bundle build workflow; no Google Play submission or approval implied.

## 1.1.1

- Address independently reviewed SSID quoting, service stop races, permission checks and stale error notifications.
- Clarify setting-value status versus actual ADB connectivity and background timing.
- Restrict boot actions, handle service-start failures and exclude standard data transfer.
- Fix SDK setup in CI and keep standalone test output temporary.

## 1.1.0

- Initial public source release under MIT.
- User-selected SSID/BSSID, stopped by default, and fail-closed identity validation.
- Separate public application ID and unsigned release builds.
- Portable verification and setup/privacy/security documentation.
- Private device logs, helpers, network identifiers and signing material excluded.
