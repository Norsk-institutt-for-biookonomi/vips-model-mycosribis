/*
 * Copyright (c) 2026 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of MycosphaerellaRibisModel.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package no.nibio.vips.model.mycosribis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.util.DateMap;
import no.nibio.vips.util.InvalidAggregationTypeException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;

/**
 * Class for calculations related to the Mycosphaerella Ribis model.
 * 
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ModelCalculations {

    // Constants for Mills table calculations
    private final static Double LEAF_WETNESS_THRESHOLD = 20d;
    private final static Integer MAX_LEAF_DRYING_TIME = 8;

    /**
     * Determines if a day is considered a "dry day" based on precipitation and leaf wetness duration.
     * @param precipitation daily precipitation in mm
     * @param leafWetnessDuration duration of leaf wetness in hours
     * @return
     */
    public boolean isDryDay(Double precipitation, Long leafWetnessDuration) {
        return precipitation < 0.2 && leafWetnessDuration < 12;
    }

    /**
     * 
     * @param hourlyLW List of hourly LW values in minutes per hour
     * @return 
     */
    public Long getDailyLW(List<WeatherObservation> hourlyLW) {
        
        // Items must be LW values in minutes per hour (0-60)
        // Collect all hourly values that are LW and value in range 0-60
        List<WeatherObservation> qualifyingObservations = hourlyLW.stream().filter(obs -> {
            return 
                obs.getLogIntervalId() == WeatherObservation.LOG_INTERVAL_ID_1H
                && WeatherElements.LEAF_WETNESS_ALL_VARIANTS.contains(obs.getElementMeasurementTypeId())
                && obs.getValue() >= 0.0 && obs.getValue() <= 60.0;
        }).collect(Collectors.toList());
        
        // List must be at least 15 items long
        if (qualifyingObservations.size() < 15) {
            throw new IllegalArgumentException("List of hourly LW observations must contain at least 15 valid items.");
        }
        // List must contain values that are no longer than 25 hours apart (DST day)
        Collections.sort(qualifyingObservations);
        long timeDifference = qualifyingObservations.get(qualifyingObservations.size() - 1).getTimeMeasured().getTime() - qualifyingObservations.get(0).getTimeMeasured().getTime();
        if (timeDifference > 25L * 60 * 60 * 1000) {
            throw new IllegalArgumentException("List of hourly LW observations must contain values that are no longer than 25 hours apart (DST accounted).");
        }
        return qualifyingObservations.stream().filter(val-> val.getValue() == 60.0).count();
    }

    /**
     * calculate consecutive dry days based on daily precipitation and leaf wetness data in the Data Matrix
     * Stores the number of consecutive dry days in the data matrix
     * Calculates the spore maturation for conidia and ascospores, stores results in the data matrix
     * @param dataMatrix
     */
    public void calculateSporeMaturation(DateMap dataMatrix, TimeZone timeZone, Date startDateSporeMaturation) {
        Date endDate = dataMatrix.getLastDateWithValues();
        Integer thresholdConsecuticeDryDaysAscospores = 7;
        Integer thresholdConsecuticeDryDaysConidia = 4;
        int consecutiveDryDays = 0;
        Double DDSumAscospores = 0.0;
        Double DDSumConidia = 0.0;
        
        Date currentDate = startDateSporeMaturation;
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(currentDate);
        //System.out.println(dataMatrix.toCSV());
        while(currentDate.compareTo(endDate) <= 0)
        {
            if(this.isDryDay(dataMatrix.getParamDoubleValueForDate(currentDate, WeatherElements.PRECIPITATION), dataMatrix.getParamDoubleValueForDate(currentDate, DataMatrix.DAILY_LW_HOURS).longValue()))
            {
                consecutiveDryDays++;
            }
            else
            {
                consecutiveDryDays = 0;
            }
            dataMatrix.setParamIntValueForDate(currentDate, DataMatrix.CONSECUTIVE_DRY_DAYS, consecutiveDryDays);

            DDSumAscospores += consecutiveDryDays < thresholdConsecuticeDryDaysAscospores ? (dataMatrix.getParamDoubleValueForDate(currentDate, WeatherElements.TEMPERATURE_MEAN)) : 0.0;
            DDSumConidia += consecutiveDryDays < thresholdConsecuticeDryDaysConidia ? (dataMatrix.getParamDoubleValueForDate(currentDate, WeatherElements.TEMPERATURE_MEAN)) : 0.0;

            dataMatrix.setParamDoubleValueForDate(currentDate, DataMatrix.DD_SUM_ASCOSPORES, DDSumAscospores);
            dataMatrix.setParamDoubleValueForDate(currentDate, DataMatrix.DD_SUM_CONIDIA, DDSumConidia);

            dataMatrix.setParamDoubleValueForDate(currentDate, DataMatrix.PROPORTION_MATURE_ASCOSPORES, this.getProportionMatureAscospores(DDSumAscospores));
            dataMatrix.setParamDoubleValueForDate(currentDate, DataMatrix.PROPORTION_MATURE_CONIDIA, this.getProportionMatureConidia(DDSumConidia));

            cal.add(Calendar.DATE, 1);
            currentDate = cal.getTime();
        }
    }


    /**
     * B   = [9.235    0.012];
     * r_d = exp(-B(1)*exp(-B(2)*DD));
     * @param DDSumAscospores
     * @return
     */
    public Double getProportionMatureAscospores(Double DDSumAscospores) {
        return Math.exp(-9.235 * Math.exp(-0.012 * DDSumAscospores));
    }

    /**
     * B   = [2.1736    0.0130];
     * r_d = exp(-B(1)*exp(-B(2)*DD));
     * @param DDSumConidia
     * @return
     */
    public Double getProportionMatureConidia(Double DDSumConidia) {
        return Math.exp(-2.1736 * Math.exp(-0.013 * DDSumConidia));
    }

    /**
     * Initialize the data matrix, set the weather data
     * @param allObservations
     * @return
     */
    public DataMatrix initDataMatrix(List<WeatherObservation> allObservations) {
        DataMatrix dataMatrix = new DataMatrix();
        for(WeatherObservation obs : allObservations) {
            dataMatrix.setParamDoubleValueForDate(obs.getTimeMeasured(), obs.getElementMeasurementTypeId(), obs.getValue());
        }   
        return dataMatrix;
    }

    /**
     * Checks that the provided weather data contains the necessary observations for the model
     * Required: Hourly values for TM and BT, hourly or daily for RR
     * @param observations
     */
    public Map<Integer,List<WeatherObservation>> validateAndInitializeWeatherData(List<WeatherObservation> observations, TimeZone timeZone) throws ConfigValidationException{
        WeatherUtil weatherUtil = new WeatherUtil();
        // Temperature - must be hourly
        List<String> acceptedTypes = List.of(WeatherElements.TEMPERATURE_MEAN, WeatherElements.TEMPERATURE_INSTANTANEOUS);
        List<WeatherObservation> TMh = observations.stream().filter(obs -> acceptedTypes.contains(obs.getElementMeasurementTypeId())).collect(Collectors.toList());
        List<WeatherObservation> TMd;
        // List cannot be empty
        if (TMh.isEmpty()) {
            throw new ConfigValidationException("Temperature observations must contain at least one valid item.");
        }
        // Must be hourly
        if(TMh.stream().anyMatch(obs -> obs.getLogIntervalId() == WeatherObservation.LOG_INTERVAL_ID_1D )) {
            throw new ConfigValidationException("Temperature weather data must be hourly");
        }
        // Values must be in range -50 to 60 degrees Celsius
        if(TMh.stream().anyMatch(obs -> obs.getValue() < -50.0 || obs.getValue() > 60.0)) {
            throw new ConfigValidationException("Temperature observations must have values between -50 and 60 degrees Celsius.");
        }
        // Must contain at least 15 hourly observations/day with valid (mean or instantaneous) temperature values 
        try {
            TMd = weatherUtil.getAggregatedDailyValues(TMh, timeZone, 15, WeatherUtil.AGGREGATION_TYPE_AVERAGE);
        } catch (WeatherObservationListException | InvalidAggregationTypeException e) {
            throw new ConfigValidationException(e.getMessage());
        }
       

        // Precipitation
        List<WeatherObservation> RRh = observations.stream().filter(obs->obs.getElementMeasurementTypeId().equals(WeatherElements.PRECIPITATION)).collect(Collectors.toList());
        List<WeatherObservation> RRd;
        if(RRh.isEmpty())
        {
            throw new ConfigValidationException("Precipitation observations must contain at least one valid item.");
        }
        // All values must be >= -0.1 mm
        if(RRh.stream().anyMatch(obs-> obs.getValue() < -0.01 || obs.getValue() > 500))
        {
            throw new ConfigValidationException("Precipitation values cannot be negative or more than 500 mm");
        }
        // Must contain at least 15 hourly observations per day to aggregate
        try {
            RRd = weatherUtil.getAggregatedDailyValues(RRh, timeZone, 15, WeatherUtil.AGGREGATION_TYPE_SUM);
        } catch (WeatherObservationListException | InvalidAggregationTypeException e) {
            throw new ConfigValidationException(e.getMessage());
        }


        // Leaf Wetness        
        List<WeatherObservation> BTd = new ArrayList<>();
        List<WeatherObservation> BTh = observations.stream().filter(obs-> WeatherElements.LEAF_WETNESS_ALL_VARIANTS.contains(obs.getElementMeasurementTypeId())).collect(Collectors.toList());
        if(BTh.isEmpty())
        {
            throw new ConfigValidationException("Leaf wetness observations must contain at least one valid item.");
        }
        // If hourly: Must contain at least 15 hourly observations/day with valid leaf wetness values (e.g., 0 to 60 minutes per hour)
        if(BTh.stream().anyMatch(obs -> obs.getLogIntervalId() == WeatherObservation.LOG_INTERVAL_ID_1H))
        {
            if(BTh.stream().anyMatch(obs -> obs.getValue() < 0.0 || obs.getValue() > 60.0)) {
                throw new ConfigValidationException("Hourly leaf wetness observations must have values between 0 and 60 minutes per hour.");
            }
            // Convert hourly leaf wetness to daily leaf wetness duration in hours
            // using the getDailyLW method defined above, which counts the number of hours with leaf wetness = 60 minutes
            Map<Date, List<WeatherObservation>> hourlyValuesPerDay = weatherUtil.getHourlyValuesPerDay(BTh, timeZone);
            
            for (Date day : hourlyValuesPerDay.keySet()) {
                List<WeatherObservation> hourlyLWForDay = hourlyValuesPerDay.get(day);
                Long dailyLWHours = getDailyLW(hourlyLWForDay);
                // Here you would store the dailyLWHours in your data structure as needed
                WeatherObservation dailyLWObservation = new WeatherObservation();
                dailyLWObservation.setTimeMeasured(day);
                dailyLWObservation.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1D);
                dailyLWObservation.setElementMeasurementTypeId(DataMatrix.DAILY_LW_HOURS);
                dailyLWObservation.setValue(dailyLWHours.doubleValue());
                BTd.add(dailyLWObservation);
            }
        }
        // Raw BT values must be hourly
        if(BTh.stream().anyMatch(obs -> obs.getLogIntervalId() == WeatherObservation.LOG_INTERVAL_ID_1D)) {
            throw new ConfigValidationException("Leaf wetness weather data must be hourly");
        }

        // Check that the daily lists have equal length
        if (!(TMd.size() == RRd.size() && RRd.size() == BTd.size())) {
            throw new ConfigValidationException("Temperature, precipitation, and leaf wetness observations must have the same number of valid items.");
        }
        // Check that the lists start and end on the same date
        if (!(TMd.get(0).getTimeMeasured().equals(RRd.get(0).getTimeMeasured()) && RRd.get(0).getTimeMeasured().equals(BTd.get(0).getTimeMeasured()))) {
            throw new ConfigValidationException("Temperature (" + TMd.get(0).getTimeMeasured() + "), precipitation(" + RRd.get(0).getTimeMeasured() + "), and leaf wetness (" + BTd.get(0).getTimeMeasured() + ") observations must start on the same date.");
        }
        if (!(TMd.get(TMd.size() - 1).getTimeMeasured().equals(RRd.get(RRd.size() - 1).getTimeMeasured()) && RRd.get(RRd.size() - 1).getTimeMeasured().equals(BTd.get(BTd.size() - 1).getTimeMeasured()))) {
            throw new ConfigValidationException("Temperature, precipitation, and leaf wetness observations must end on the same date.");
        }
        
        // Combine all validated daily observations into a single list
        List<WeatherObservation> validatedObservations = new ArrayList<>();
        validatedObservations.addAll(TMd);
        validatedObservations.addAll(RRd);
        validatedObservations.addAll(BTd);
        Map<Integer, List<WeatherObservation>> retVal = new HashMap<>();
        retVal.put(WeatherObservation.LOG_INTERVAL_ID_1D, validatedObservations);
        // Adding hourly observations for TM and BT for Mills table
        TMh.addAll(BTh);
        TMh.addAll(RRh);
        retVal.put(WeatherObservation.LOG_INTERVAL_ID_1H, TMh);
        return retVal;
    }

    /**
     * Loops through dates, calculates Mill's value for each hour and accumulates
     * For this model, we only use the Ascospore part of Mills table
     * Depending on hourly values for weather data
     * @throws WeatherObservationListException 
     */
    public DataMatrix accumulateMills(DataMatrix hourlyData, DataMatrix dailyData, Date startDateSporeMaturation, TimeZone timeZone) throws WeatherObservationListException
    {
        WeatherUtil weatherUtil = new WeatherUtil();
        Double accumulatedMills = 0d;
        Double ascosporeMaturity = 0d;
        // We have already calculated this
        //AscosporeMaturityTable ascosporeMaturityTable = new AscosporeMaturityTable();
        MillsTable millsTable = new MillsTable();
        Date currentTimeStamp = startDateSporeMaturation;
        Date endTimeStamp = hourlyData.getLastDateWithParameterValue(WeatherElements.TEMPERATURE_MEAN);
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(currentTimeStamp);
        Integer hoursSinceLeafWetness = 0;
        while(currentTimeStamp.compareTo(endTimeStamp) <= 0 && ascosporeMaturity < 100)
        {
            // We update and store the ascospore maturity only at midnight, when we have an updated accumulated temperature
            if(currentTimeStamp.equals(weatherUtil.normalizeToExactDate(currentTimeStamp, timeZone)))
            {
                ascosporeMaturity = dailyData.getParamDoubleValueForDate(currentTimeStamp, DataMatrix.PROPORTION_MATURE_ASCOSPORES);
                hourlyData.setParamDoubleValueForDate(currentTimeStamp, DataMatrix.PROPORTION_MATURE_ASCOSPORES, ascosporeMaturity);
            }


            // We accumulate Mills only when there are humid conditions
            Double currentLeafWetness = hourlyData.getParamDoubleValueForDate(currentTimeStamp, WeatherElements.LEAF_WETNESS_DURATION);
            if(currentLeafWetness != null && currentLeafWetness < LEAF_WETNESS_THRESHOLD)
            {
                hoursSinceLeafWetness++;
                // If dry for long enough, reset accumulated Mills
                if(hoursSinceLeafWetness >= MAX_LEAF_DRYING_TIME)
                {
                    accumulatedMills = 0d;
                }
            }
            else
            {
                hoursSinceLeafWetness = 0;
                Double currentTemperature = hourlyData.getParamDoubleValueForDate(currentTimeStamp, WeatherElements.TEMPERATURE_MEAN);
                if(currentTemperature != null)
                {
                    accumulatedMills += millsTable.getMillsValue(currentTemperature, 1);
                }
            }

            hourlyData.setParamDoubleValueForDate(currentTimeStamp, DataMatrix.ACCUMULATED_MILLS, accumulatedMills);
            
            cal.add(Calendar.HOUR, 1);
            currentTimeStamp = cal.getTime();
        }
        return hourlyData;
    }
} 
