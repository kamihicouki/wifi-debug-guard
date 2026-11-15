# Changelog

## 1.3.0

- Separate live management, current Android debugging setting, Wi-Fi matching and required setup into dashboard cards.
- Make starting management also enable startup on boot; confirmed stop disables both and requests debugging OFF.
- Add an independent boot-start switch that leaves current management unchanged, with fallback to the previous enabled setting on upgrade.
- Check consent, saved target, permissions and location before boot startup; resume app updates only when management is enabled.
- Register a new Wi-Fi target without stopping management or changing debugging settings while stopped.
- Add an application-ID build override for compatible updates of the private device package, and expand pure policy/state checks to 54 cases.
- Device startup and reboot acceptance remain unconfirmed; no Play submission or publication implied.

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
