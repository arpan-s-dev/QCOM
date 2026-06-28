"""
Solar position calculator (NOAA/Meeus algorithm), verified here in Python
before porting to Kotlin (SolarCompass.kt). Computes sun azimuth and
elevation for a given lat/lon/UTC time.

Sanity checks required by the task, for ~37.3N, 121.9W (San Jose area) in
late June:
  - solar noon should put the sun roughly due south (azimuth ~180)
  - sunrise should be roughly ENE (azimuth ~60-70)
  - sunset should be roughly WNW (azimuth ~290-300)
  - elevation should be negative at night (sun below horizon)

Run: python3 scripts/verify_solar_math.py
"""

import math
from datetime import datetime, timedelta, timezone


def julian_day(dt_utc: datetime) -> float:
    """Julian Day Number for a UTC datetime."""
    y, m, d = dt_utc.year, dt_utc.month, dt_utc.day
    frac_day = (dt_utc.hour + dt_utc.minute / 60 + dt_utc.second / 3600) / 24
    if m <= 2:
        y -= 1
        m += 12
    a = y // 100
    b = 2 - a + a // 4
    jd = math.floor(365.25 * (y + 4716)) + math.floor(30.6001 * (m + 1)) + d + frac_day + b - 1524.5
    return jd


def solar_position(dt_utc: datetime, lat_deg: float, lon_deg: float):
    """
    Returns (azimuth_deg, elevation_deg). Azimuth measured clockwise from
    true north (0=N, 90=E, 180=S, 270=W) -- standard compass convention.

    Based on the NOAA Solar Calculator algorithm (itself based on
    Meeus, "Astronomical Algorithms"), simplified for use without
    atmospheric refraction correction (fine for a hiking compass, not for
    precision astronomy).
    """
    jd = julian_day(dt_utc)
    jc = (jd - 2451545.0) / 36525.0  # Julian century

    # Geometric mean longitude of the sun (deg)
    geom_mean_long_sun = (280.46646 + jc * (36000.76983 + jc * 0.0003032)) % 360

    # Geometric mean anomaly of the sun (deg)
    geom_mean_anom_sun = 357.52911 + jc * (35999.05029 - 0.0001537 * jc)

    # Eccentricity of earth's orbit
    eccent_earth_orbit = 0.016708634 - jc * (0.000042037 + 0.0000001267 * jc)

    # Equation of center
    mrad = math.radians(geom_mean_anom_sun)
    sun_eq_of_center = (
        math.sin(mrad) * (1.914602 - jc * (0.004817 + 0.000014 * jc))
        + math.sin(2 * mrad) * (0.019993 - 0.000101 * jc)
        + math.sin(3 * mrad) * 0.000289
    )

    sun_true_long = geom_mean_long_sun + sun_eq_of_center

    # Apparent longitude of the sun (deg), correcting for nutation/aberration
    omega = 125.04 - 1934.136 * jc
    sun_app_long = sun_true_long - 0.00569 - 0.00478 * math.sin(math.radians(omega))

    # Mean obliquity of the ecliptic (deg)
    mean_obliq_ecliptic = (
        23 + (26 + (21.448 - jc * (46.815 + jc * (0.00059 - jc * 0.001813))) / 60) / 60
    )
    obliq_corr = mean_obliq_ecliptic + 0.00256 * math.cos(math.radians(omega))

    # Sun's declination (deg)
    sun_declin = math.degrees(
        math.asin(math.sin(math.radians(obliq_corr)) * math.sin(math.radians(sun_app_long)))
    )

    # Equation of time (minutes)
    y_ = math.tan(math.radians(obliq_corr / 2)) ** 2
    eq_time = 4 * math.degrees(
        y_ * math.sin(2 * math.radians(geom_mean_long_sun))
        - 2 * eccent_earth_orbit * math.sin(mrad)
        + 4 * eccent_earth_orbit * y_ * math.sin(mrad) * math.cos(2 * math.radians(geom_mean_long_sun))
        - 0.5 * y_ * y_ * math.sin(4 * math.radians(geom_mean_long_sun))
        - 1.25 * eccent_earth_orbit * eccent_earth_orbit * math.sin(2 * mrad)
    )

    # True solar time (minutes)
    time_offset_minutes = dt_utc.hour * 60 + dt_utc.minute + dt_utc.second / 60
    solar_time_min = (time_offset_minutes + eq_time + 4 * lon_deg) % 1440

    # Hour angle (deg)
    hour_angle = (solar_time_min / 4) - 180 if solar_time_min >= 0 else (solar_time_min / 4) + 180

    lat_rad = math.radians(lat_deg)
    declin_rad = math.radians(sun_declin)
    hour_angle_rad = math.radians(hour_angle)

    # Solar zenith angle -> elevation
    cos_zenith = (
        math.sin(lat_rad) * math.sin(declin_rad)
        + math.cos(lat_rad) * math.cos(declin_rad) * math.cos(hour_angle_rad)
    )
    cos_zenith = max(-1.0, min(1.0, cos_zenith))
    zenith_rad = math.acos(cos_zenith)
    elevation_deg = 90 - math.degrees(zenith_rad)

    # Azimuth (clockwise from north)
    elevation_rad = math.radians(elevation_deg)
    cos_az = (
        (math.sin(declin_rad) - math.sin(lat_rad) * math.sin(elevation_rad))
        / (math.cos(lat_rad) * math.cos(elevation_rad))
    ) if math.cos(lat_rad) * math.cos(elevation_rad) != 0 else 0
    cos_az = max(-1.0, min(1.0, cos_az))
    az_rad = math.acos(cos_az)
    azimuth_deg = math.degrees(az_rad)

    if hour_angle > 0:
        azimuth_deg = 360 - azimuth_deg

    return azimuth_deg, elevation_deg


def find_solar_noon_utc(date_local_noon_guess_utc: datetime, lat: float, lon: float) -> datetime:
    """Scan across the day in 1-minute steps to find max elevation (solar noon)."""
    best_time = None
    best_elev = -999
    start = date_local_noon_guess_utc.replace(hour=0, minute=0, second=0, microsecond=0)
    for minutes in range(0, 24 * 60):
        t = start + timedelta(minutes=minutes)
        _, elev = solar_position(t, lat, lon)
        if elev > best_elev:
            best_elev = elev
            best_time = t
    return best_time, best_elev


def find_crossing(date_utc: datetime, lat: float, lon: float, rising: bool):
    """Find approx sunrise (rising=True) or sunset (rising=False) time and azimuth."""
    start = date_utc.replace(hour=0, minute=0, second=0, microsecond=0)
    prev_elev = None
    for minutes in range(0, 24 * 60):
        t = start + timedelta(minutes=minutes)
        az, elev = solar_position(t, lat, lon)
        if prev_elev is not None:
            if rising and prev_elev < 0 <= elev:
                return t, az, elev
            if not rising and prev_elev >= 0 > elev:
                return t, az, elev
        prev_elev = elev
    return None, None, None


if __name__ == "__main__":
    LAT, LON = 37.3, -121.9  # San Jose area
    # Use a late-June date for the sanity check (PDT = UTC-7)
    test_date_utc = datetime(2026, 6, 21, 0, 0, 0, tzinfo=timezone.utc)

    print(f"=== Solar sanity check: lat={LAT}, lon={LON}, date=2026-06-21 (late June) ===\n")

    noon_time, noon_elev = find_solar_noon_utc(test_date_utc, LAT, LON)
    noon_az, _ = solar_position(noon_time, LAT, LON)
    noon_local = noon_time - timedelta(hours=7)  # display only: UTC-7 ~ PDT, no DST table used
    print(f"Solar noon (max elevation): {noon_local} [UTC-7, approx PDT for display]")
    print(f"  azimuth={noon_az:.1f} deg (expect ~180, due south)")
    print(f"  elevation={noon_elev:.1f} deg (expect high, near max for the year)")
    noon_ok = abs(noon_az - 180) < 5
    print(f"  CHECK: {'PASS' if noon_ok else 'FAIL'} (azimuth within 5 deg of 180)\n")

    sunrise_time, sunrise_az, sunrise_elev = find_crossing(test_date_utc, LAT, LON, rising=True)
    sunrise_local = sunrise_time - timedelta(hours=7)
    print(f"Sunrise: {sunrise_local} [UTC-7, approx PDT for display]")
    print(f"  azimuth={sunrise_az:.1f} deg (expect ~60-70, ENE in late June at this latitude)")
    sunrise_ok = 50 <= sunrise_az <= 80
    print(f"  CHECK: {'PASS' if sunrise_ok else 'FAIL'} (azimuth in ENE range)\n")

    sunset_time, sunset_az, sunset_elev = find_crossing(test_date_utc, LAT, LON, rising=False)
    sunset_local = sunset_time - timedelta(hours=7)
    print(f"Sunset: {sunset_local} [UTC-7, approx PDT for display]")
    print(f"  azimuth={sunset_az:.1f} deg (expect ~280-300, WNW in late June at this latitude)")
    sunset_ok = 270 <= sunset_az <= 310
    print(f"  CHECK: {'PASS' if sunset_ok else 'FAIL'} (azimuth in WNW range)\n")

    midnight_local_as_utc = test_date_utc + timedelta(hours=7)  # ~midnight PDT in UTC terms
    midnight_az, midnight_elev = solar_position(midnight_local_as_utc, LAT, LON)
    print(f"Midnight check: elevation={midnight_elev:.1f} deg (expect negative, sun below horizon)")
    midnight_ok = midnight_elev < 0
    print(f"  CHECK: {'PASS' if midnight_ok else 'FAIL'} (elevation negative at night)\n")

    all_pass = noon_ok and sunrise_ok and sunset_ok and midnight_ok
    print(f"=== {'ALL SANITY CHECKS PASSED' if all_pass else 'SOME CHECKS FAILED'} ===")
    if not all_pass:
        raise SystemExit(1)
