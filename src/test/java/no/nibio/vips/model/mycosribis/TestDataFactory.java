/*
 * Copyright (c) 2026 NIBIO <http://www.nibio.no/>.
 */
package no.nibio.vips.model.mycosribis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.util.WeatherElements;

public class TestDataFactory {

    static String timeZoneDef = "Europe/Oslo";

    public static List<WeatherObservation> buildDailyPrecipitation(double[] values, Date startDate) {
        return build(values, startDate, WeatherElements.PRECIPITATION);
    }

    public static List<WeatherObservation> buildDailyLeafWetness(double[] values, Date startDate) {
        return build(values, startDate, WeatherElements.LEAF_WETNESS_DURATION);
    }

    public static List<WeatherObservation> buildDailyTemperature(double[] values, Date startDate) {
        return build(values, startDate, WeatherElements.TEMPERATURE_MEAN);
    }

    private static List<WeatherObservation> build(double[] values, Date startDate, String elementId) {
        List<WeatherObservation> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneDef));
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        for (double v : values) {
            WeatherObservation obs = new WeatherObservation();
            obs.setTimeMeasured(cal.getTime());
            obs.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1D);
            obs.setElementMeasurementTypeId(elementId);
            obs.setValue(v);
            list.add(obs);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return list;
    }

    public static Date dateOf(int year, int month, int day) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneDef));
        cal.clear();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
