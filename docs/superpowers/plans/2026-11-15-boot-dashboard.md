# Boot and status dashboard implementation plan

**Goal:** Start configured management at device boot and clearly distinguish management, live Android debugging setting, Wi-Fi matching and missing setup.
**Architecture:** A side-effect-free GuardSnapshot reads current settings and own-service presence. Pure boot/presentation policies drive a native Java dashboard; boot preference is separate from the current management request.
**Constraints:** Preserve application data and existing C2 signing identity; no private identifiers in Git; Android 13/37 devices; respect Pixel relay lock and active PayPay work. No Play submission or support messages.

- [x] Test and implement boot decision: initial setup never starts, boot auto-start can resume, explicit persistent stop prevents restart, package update only resumes active management.
- [x] Test presentation distinctions: requested/running combinations, ON/OFF/unreadable setting, configured/disconnected/redacted/matching/other Wi-Fi.
- [x] Implement native status cards, immediate refresh after controls, setting observer, independent boot switch and persistent stop labels. Keep incremental consent/permission setup.
- [x] Build generic public and signed legacy compatibility APKs; inspect package/version/signers before install.
- [ ] Install C2 in place and Pixel public package under relay exclusion after active work permits. Configure current Wi-Fi and explicit boot start without changing other apps.
- [ ] Verify UI/settings/service/readback/screenshots. Real reboot/network interruption only when competing work is idle and reconnection path is ready.
- [ ] Update version/changelog/docs, review, secret-scan and publish source. Report all unverified conditions and document/code mismatches.
