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
import java.util.Collections;
import java.util.Date;
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

    /**
     * Determines if a day is considered a "dry day" based on precipitation and leaf wetness duration.
     * @param precipitation daily precipitation in mm
     * @param leafWetnessDuration duration of leaf wetness in hours
     * @return
     */
    public boolean isDryDay(Double precipitation, Long leafWetnessDuration) {
        return precipitation < 0.2 || leafWetnessDuration < 12;
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
        // List must contain values that are no longer than 24 hours apart
        Collections.sort(qualifyingObservations);
        long timeDifference = qualifyingObservations.get(qualifyingObservations.size() - 1).getTimeMeasured().getTime() - qualifyingObservations.get(0).getTimeMeasured().getTime();
        if (timeDifference > 24 * 60 * 60 * 1000) {
            throw new IllegalArgumentException("List of hourly LW observations must contain values that are no longer than 24 hours apart.");
        }
        return qualifyingObservations.stream().filter(val-> val.getValue() == 60.0).count();
    }

    public void calculateConsecutiveDryDays(DateMap dataMatrix) {
        // This method would calculate consecutive dry days based on the provided daily precipitation and leaf wetness data.
        // Implementation would depend on how the data is structured and how you want to define "consecutive".
        Date startDate = dataMatrix.getFirstDateWithValues();
        Date endDate = dataMatrix.getLastDateWithValues();
        int consecutiveDryDays = 0;
        
    }

    public DateMap initDataMatrix(List<WeatherObservation> allObservations) {
        // This method would initialize the data matrix with the necessary calculations for the model.
        // It would likely involve iterating through the daily precipitation and hourly LW observations, calculating daily LW, and determining dry days.
        DateMap dataMatrix = new DateMap();
        for(WeatherObservation obs : allObservations) {
            dataMatrix.setParamDoubleValueForDate(obs.getTimeMeasured(), obs.getElementMeasurementTypeId(), obs.getValue());
        }   
        return dataMatrix;
    }

    /**
     * Checks that the provided weather data contains the necessary observations for the model and initializes the data matrix.
     * @param observations
     */
    public List<WeatherObservation> validateAndInitializeWeatherData(List<WeatherObservation> observations, TimeZone timeZone) throws ConfigValidationException{
        WeatherUtil weatherUtil = new WeatherUtil();
        // Temperature
        List<String> acceptedTypes = List.of(WeatherElements.TEMPERATURE_MEAN, WeatherElements.TEMPERATURE_INSTANTANEOUS);
        List<WeatherObservation> TM = observations.stream().filter(obs -> acceptedTypes.contains(obs.getElementMeasurementTypeId())).collect(Collectors.toList());
        // List cannot be empty
        if (TM.isEmpty()) {
            throw new ConfigValidationException("Temperature observations must contain at least one valid item.");
        }
        // If daily: must be mean temperature
        if(TM.stream().anyMatch(obs -> obs.getLogIntervalId() == WeatherObservation.LOG_INTERVAL_ID_1D && obs.getElementMeasurementTypeId() != WeatherElements.TEMPERATURE_MEAN)) {
            throw new ConfigValidationException("Daily temperature observations must be mean temperature.");
        }
        // Values must be in range -50 to 60 degrees Celsius
        if(TM.stream().anyMatch(obs -> obs.getValue() < -50.0 || obs.getValue() > 60.0)) {
            throw new ConfigValidationException("Temperature observations must have values between -50 and 60 degrees Celsius.");
        }
        // If hourly: Must contain at least 15 hourly observations/day with valid (mean or instantaneous) temperature values 
        if(TM.stream().anyMatch(obs -> obs.getLogIntervalId() == WeatherObservation.LOG_INTERVAL_ID_1H))
        {
            try {
                TM = weatherUtil.getAggregatedDailyValues(observations, timeZone, 15, WeatherUtil.AGGREGATION_TYPE_AVERAGE);
            } catch (WeatherObservationListException | InvalidAggregationTypeException e) {
                throw new ConfigValidationException(e.getMessage());
            }
        }

        // Precipitation
        List<WeatherObservation> RR = observations.stream().filter(obs->obs.getElementMeasurementTypeId().equals(WeatherElements.PRECIPITATION)).collect(Collectors.toList());
        if(RR.isEmpty())
        {
            throw new ConfigValidationException("Precipitation observations must contain at least one valid item.");
        }
        // All values must be >= -0.1 mm
        if(RR.stream().anyMatch(obs-> obs.getValue() < -0.01 || obs.getValue() > 500))
        {
            throw new ConfigValidationException("Precipitation values cannot be negative or more than 500 mm");
        }
        // If hourly: Must contain at least 15 hourly observations
        if(RR.stream().anyMatch(obs -> obs.getLogIntervalId() == WeatherObservation.LOG_INTERVAL_ID_1H))
        {
            try {
                RR = weatherUtil.getAggregatedDailyValues(observations, timeZone, 15, WeatherUtil.AGGREGATION_TYPE_SUM);
            } catch (WeatherObservationListException | InvalidAggregationTypeException e) {
                throw new ConfigValidationException(e.getMessage());
            }
        }

        // Leaf Wetness        
        List<WeatherObservation> BT = observations.stream().filter(obs-> WeatherElements.LEAF_WETNESS_ALL_VARIANTS.contains(obs.getElementMeasurementTypeId())).collect(Collectors.toList());
        if(BT.isEmpty())
        {
            throw new ConfigValidationException("Leaf wetness observations must contain at least one valid item.");
        }
        // If hourly: Must contain at least 15 hourly observations/day with valid leaf wetness values (e.g., 0 to 60 minutes per hour)
        if(BT.stream().anyMatch(obs -> obs.getLogIntervalId() == WeatherObservation.LOG_INTERVAL_ID_1H))
        {
            if(BT.stream().anyMatch(obs -> obs.getValue() < 0.0 || obs.getValue() > 60.0)) {
                throw new ConfigValidationException("Hourly leaf wetness observations must have values between 0 and 60 minutes per hour.");
            }
            // Convert hourly leaf wetness to daily leaf wetness duration in hours
            // using the getDailyLW method defined above, which counts the number of hours with leaf wetness = 60 minutes
            Map<Date, List<WeatherObservation>> hourlyValuesPerDay = weatherUtil.getHourlyValuesPerDay(BT, timeZone);
            BT.clear();
            for (Date day : hourlyValuesPerDay.keySet()) {
                List<WeatherObservation> hourlyLWForDay = hourlyValuesPerDay.get(day);
                Long dailyLWHours = getDailyLW(hourlyLWForDay);
                // Here you would store the dailyLWHours in your data structure as needed
                WeatherObservation dailyLWObservation = new WeatherObservation();
                dailyLWObservation.setTimeMeasured(day);
                dailyLWObservation.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1D);
                dailyLWObservation.setElementMeasurementTypeId(WeatherElements.LEAF_WETNESS_DURATION);
                dailyLWObservation.setValue(dailyLWHours.doubleValue());
                BT.add(dailyLWObservation);
            }
        }
        // If daily: Value must be in range 0-24 (25 for DST day)
        else if(BT.stream().anyMatch(obs -> obs.getLogIntervalId() == WeatherObservation.LOG_INTERVAL_ID_1D && (obs.getValue() < 0.0 || obs.getValue() > 25.0))) {
            throw new ConfigValidationException("Daily leaf wetness observations must have values between 0 and 24 hours (25 for DST day).");
        }

        // Check that the lists have equal length
        if (!(TM.size() == RR.size() && RR.size() == BT.size())) {
            throw new ConfigValidationException("Temperature, precipitation, and leaf wetness observations must have the same number of valid items.");
        }
        // Check that the lists start and end on the same date
        if (!(TM.get(0).getTimeMeasured().equals(RR.get(0).getTimeMeasured()) && RR.get(0).getTimeMeasured().equals(BT.get(0).getTimeMeasured()))) {
            throw new ConfigValidationException("Temperature, precipitation, and leaf wetness observations must start on the same date.");
        }
        if (!(TM.get(TM.size() - 1).getTimeMeasured().equals(RR.get(RR.size() - 1).getTimeMeasured()) && RR.get(RR.size() - 1).getTimeMeasured().equals(BT.get(BT.size() - 1).getTimeMeasured()))) {
            throw new ConfigValidationException("Temperature, precipitation, and leaf wetness observations must end on the same date.");
        }
        
        // Combine all validated observations into a single list and return
        List<WeatherObservation> validatedObservations = new ArrayList<>();
        validatedObservations.addAll(TM);
        validatedObservations.addAll(RR);
        validatedObservations.addAll(BT);
        return validatedObservations;
    }
} 
