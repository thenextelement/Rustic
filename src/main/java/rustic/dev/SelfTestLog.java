package rustic.dev;

import rustic.core.Rustic;

/** Result counting / logging for the self-test; no client classes, so it also loads on a dedicated server. */
final class SelfTestLog {

    static int failures;
    static int passes;

    private SelfTestLog() {}

    static void log(String msg, Object... args) {
        Rustic.logger.info("SELFTEST " + msg, args);
    }

    static void check(boolean ok, String what, Object... args) {
        if (ok) {
            passes++;
        } else {
            failures++;
            Rustic.logger.error("SELFTEST FAIL " + what, args);
        }
    }

    static void fail(String what, Throwable t) {
        failures++;
        Rustic.logger.error("SELFTEST FAIL " + what, t);
    }

    static void done() {
        log("DONE: {} passed, {} failed", passes, failures);
    }
}
