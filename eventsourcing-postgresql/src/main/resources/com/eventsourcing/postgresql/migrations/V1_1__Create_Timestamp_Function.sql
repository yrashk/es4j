--
-- Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
--
-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at http://mozilla.org/MPL/2.0/.
--
CREATE TYPE hlc_timestamp AS (
  logicalCounter BIGINT,
  logicalTime    BIGINT
);

CREATE OR REPLACE FUNCTION hybrid_timestamp(ts hlc_timestamp)
    RETURNS TIMESTAMP
    AS $$
    DECLARE
    ntp bit(64);
    seconds bigint;
    fraction bigint;
    BEGIN
      SELECT ((ts."logicalTime"::bit(64) >> 16 << 16) | (ts."logicalCounter"::bit(64) << 48 >> 48)) INTO ntp;
      seconds := ((ntp >> 32 << 32))::bit(32)::bigint;
      fraction := (ntp)::bit(32)::int;
      fraction := round(fraction::bigint / 4294967296)::bigint::bit(32)::bigint;
      IF (ntp)::bit(1) = B'1' THEN
        RETURN to_timestamp(round((-2208988800000 + (seconds::bigint * 1000) + fraction::bigint)/1000));
      ELSE
        RETURN to_timestamp(round((2085978496000 + (seconds::bigint * 1000) + fraction::bigint)/1000));
      END IF;


-- From TimeStamp, for reference:

--     /**
--      * baseline NTP time if bit-0=0 is 7-Feb-2036 @ 06:28:16 UTC
--      */
--     protected static final long msb0baseTime = 2085978496000L;
--
--     /**
--      *  baseline NTP time if bit-0=1 is 1-Jan-1900 @ 01:00:00 UTC
--      */
--     protected static final long msb1baseTime = -2208988800000L;
--
--         long seconds = (ntpTimeValue >>> 32) & 0xffffffffL;     // high-order 32-bits
--         long fraction = ntpTimeValue & 0xffffffffL;             // low-order 32-bits
--
--         // Use round-off on fractional part to preserve going to lower precision
--         fraction = Math.round(1000D * fraction / 0x100000000L);
--
--         /*
--          * If the most significant bit (MSB) on the seconds field is set we use
--          * a different time base. The following text is a quote from RFC-2030 (SNTP v4):
--          *
--          *  If bit 0 is set, the UTC time is in the range 1968-2036 and UTC time
--          *  is reckoned from 0h 0m 0s UTC on 1 January 1900. If bit 0 is not set,
--          *  the time is in the range 2036-2104 and UTC time is reckoned from
--          *  6h 28m 16s UTC on 7 February 2036.
--          */
--         long msb = seconds & 0x80000000L;
--         if (msb == 0) {
--             // use base: 7-Feb-2036 @ 06:28:16 UTC
--             return msb0baseTime + (seconds * 1000) + fraction;
--         } else {
--             // use base: 1-Jan-1900 @ 01:00:00 UTC
--             return msb1baseTime + (seconds * 1000) + fraction;
--         }

      RETURN ntp;
    END
    $$
    LANGUAGE plpgsql;

