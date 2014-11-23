/*
 * Copyright (C) 2014  Armin Häberling
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package ch.corten.aha.worldclock;

import java.text.MessageFormat;

public enum SpeedFormat {
    KiloMetersPerHour("kmh", "{0,number,#} km/h") {
        @Override
        protected double fromKmh(double speed) {
            return speed;
        }
    },
    MilesPerHour("mph", "{0,number,#} mph") {
        @Override
        protected double fromKmh(double speed) {
            return Math.round(speed * MILES_PER_KM);
        }
    },
    MetersPerSecond("ms", "{0,number,#} m/s") {
        @Override
        protected double fromKmh(double speed) {
            return Math.round(speed / KMH_PER_MS);
        }
    },
    Beaufort("beaufort", "{0,number,#} Bft") {
        @Override
        protected double fromKmh(double speed) {
            double msSpeed = speed / KMH_PER_MS;
            // CHECKSTYLE IGNORE MagicNumberCheck
            if (msSpeed < 0.3) {
                return 0;
            } else if (msSpeed <= 1.5) {
                return 1;
            } else if (msSpeed <= 3.3) {
                return 2;
            } else if (msSpeed <= 5.5) {
                return 3;
            } else if (msSpeed <= 8.0) {
                return 4;
            } else if (msSpeed <= 10.8) {
                return 5;
            } else if (msSpeed <= 13.9) {
                return 6;
            } else if (msSpeed <= 17.2) {
                return 7;
            } else if (msSpeed <= 20.7) {
                return 8;
            } else if (msSpeed <= 24.5) {
                return 9;
            } else if (msSpeed <= 28.4) {
                return 10;
            } else if (msSpeed <= 32.6) {
                return 11;
            } else {
                return 12;
            }
            // CHECKSTYLE END IGNORE MagicNumberCheck
        }
    };

    private static final double KMH_PER_MS = 3.6;
    private static final double MILES_PER_KM = 0.621371192;

    private final String mId;
    private final String mFormat;

    SpeedFormat(String id, String format) {
        mId = id;
        mFormat = format;
    }

    protected abstract double fromKmh(double speed);

    public String getId() {
        return mId;
    }

    public String format(double speedInKmh) {
        return MessageFormat.format(mFormat, fromKmh(speedInKmh));
    }

    public static SpeedFormat fromId(String id) {
        for (SpeedFormat sf : SpeedFormat.values()) {
            if (sf.mId.equals(id)) {
                return sf;
            }
        }
        throw new IllegalArgumentException("Unknown SpeedFormat id: " + id);
    }
}
