package ch.corten.aha.utils;

import net.time4j.Moment;
import net.time4j.SystemClock;
import net.time4j.ZonalClock;
import net.time4j.base.TimeSource;

/**
 * A special clock which takes into account possible adjustments of device clock done by app users
 * if they intend to compensate wrong platform timezone data.
 *
 * @author  Meno Hochschild
 */
public class PlatformClock
    implements TimeSource<Moment> {

    public static final TimeSource<Moment> INSTANCE = new PlatformClock();

    private final ZonalClock zonalClock = SystemClock.inPlatformView();

    private PlatformClock() {
        // singleton constructor
    }

    @Override
    public Moment currentTime() {
        return this.zonalClock.currentMoment();
    }
}
