# Changelog

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
