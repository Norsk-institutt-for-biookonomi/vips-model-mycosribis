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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.model.ModelId;
import no.nibio.vips.util.DateMap;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.test.WeatherDataFileReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 *
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ModelCalculationsTest {
    
    public ModelCalculationsTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of isDryDay method, of class ModelCalculations.
     */
    @Test
    public void testIsDryDay() {
        System.out.println("isDryDay");
        Double[] precVal = {0.0, 0.2, 0.3, 0.0};
        Long[] lwVal = {0l,12l,15l, 15l};
        Boolean[] expResult = {true, false, false, false};
        ModelCalculations instance = new ModelCalculations();
        for(int i=0;i<precVal.length;i++)
        {
            boolean result = instance.isDryDay(precVal[i], lwVal[i]);
            assertEquals(expResult[i], result);
        }
    }

    /**
     * Test of getDailyLW method, of class ModelCalculations.
     */
    @org.junit.jupiter.api.Test
    public void testGetDailyLW() throws Exception{
        System.out.println("getDailyLW");
        List<WeatherObservation> hourlyLW = new WeatherDataFileReader().getWeatherObservationsFromFile("/hourly_bt_24h.json");
        ModelCalculations instance = new ModelCalculations();
        Long expResult = 8L;
        Long result = instance.getDailyLW(hourlyLW);
        assertEquals(expResult, result);
        }

    

    /**
     * Test of initDataMatrix method, of class ModelCalculations.
     */
    @org.junit.jupiter.api.Test
    public void testInitDataMatrix() throws Exception{
        System.out.println("initDataMatrix");
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Oslo");
        ModelCalculations instance = new ModelCalculations();
        List<WeatherObservation> allObservations = instance.validateAndInitializeWeatherData(new WeatherDataFileReader().getWeatherObservationsFromFile("/hourly_all.json"), timeZone).get(WeatherObservation.LOG_INTERVAL_ID_1D);
        
        DateMap result = instance.initDataMatrix(allObservations);

        // Daily values must have been set for same period
        Date startTM = result.getFirstDateWithParameterValue("TM");
        Date startRR = result.getFirstDateWithParameterValue("RR");
        Date startBT = result.getFirstDateWithParameterValue(DataMatrix.DAILY_LW_HOURS);
        assertEquals(startTM, startRR);
        assertEquals(startTM, startBT);

        Date endTM = result.getLastDateWithParameterValue("TM");
        Date endRR = result.getLastDateWithParameterValue("RR");
        Date endBT = result.getLastDateWithParameterValue(DataMatrix.DAILY_LW_HOURS);
        assertEquals(endTM, endRR);
        assertEquals(endTM, endBT);

        // For each day, values for TM, BT and RR have been set
        Date currentDate = startTM;
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(currentDate);
        while(currentDate.compareTo(endTM) <= 0)
        {
            assertNotNull(result.getParamDoubleValueForDate(currentDate, WeatherElements.TEMPERATURE_MEAN));
            assertNotNull(result.getParamDoubleValueForDate(currentDate, WeatherElements.PRECIPITATION));
            assertNotNull(result.getParamDoubleValueForDate(currentDate, DataMatrix.DAILY_LW_HOURS));
            cal.add(Calendar.DATE, 1);
            currentDate = cal.getTime();
        }

    }

    /**
     * Test of validateAndInitializeWeatherData method, of class ModelCalculations.
     * What is tested: Correct aggregation of hourly values into daily values
     */
    @org.junit.jupiter.api.Test
    public void testValidateAndInitializeWeatherData() throws Exception {
        System.out.println("validateAndInitializeWeatherData");
        List<WeatherObservation> hourlyObservations = new WeatherDataFileReader().getWeatherObservationsFromFile("/hourly_all.json");
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Oslo");
        ModelCalculations instance = new ModelCalculations();
        List<WeatherObservation> expResult = new WeatherDataFileReader().getWeatherObservationsFromFile("/daily_all.json");
        List<WeatherObservation> result = instance.validateAndInitializeWeatherData(hourlyObservations, timeZone).get(WeatherObservation.LOG_INTERVAL_ID_1D);
        
        // Compare values for each parameter
        WeatherUtil wUtil = new WeatherUtil();
        // TM
        List<WeatherObservation> resultTM = wUtil.filterWeatherObservationsByParameter(result, "TM");
        List<WeatherObservation> expResultTM = wUtil.filterWeatherObservationsByParameter(expResult, "TM");
        Collections.sort(resultTM);
        Collections.sort(expResultTM);

        for(int i=0;i<expResultTM.size();i++)
        {
            assertEquals(expResultTM.get(i).getTimeMeasured(), resultTM.get(i).getTimeMeasured());
            assertEquals(expResultTM.get(i).getValue(), resultTM.get(i).getValue(), 0.25);
        }

        List<WeatherObservation> resultRR = wUtil.filterWeatherObservationsByParameter(result, "RR");
        List<WeatherObservation> expResultRR = wUtil.filterWeatherObservationsByParameter(expResult, "RR");
        Collections.sort(resultRR);
        Collections.sort(expResultRR);

        for(int i=0;i<expResultRR.size();i++)
        {
            assertEquals(expResultRR.get(i).getTimeMeasured(), resultRR.get(i).getTimeMeasured());
            assertEquals(expResultRR.get(i).getValue(), resultRR.get(i).getValue(), 0.1);
        }

        // BT
        List<WeatherObservation> resultBT = wUtil.filterWeatherObservationsByParameter(result, DataMatrix.DAILY_LW_HOURS);
        List<WeatherObservation> expResultBT = TestDataFactory.buildDailyLeafWetness(new double[]{0,0,0,0,8,8,1,0,4,24}, TestDataFactory.dateOf(2019, 5, 1));

        Collections.sort(resultBT);
        Collections.sort(expResultBT);
        for(int i=0;i<expResultBT.size();i++)
        {
            assertEquals(expResultBT.get(i).getTimeMeasured(), resultBT.get(i).getTimeMeasured());
            assertEquals(expResultBT.get(i).getValue(), resultBT.get(i).getValue());
        }
    }

    
    public void buildDailyWeatherObservationsFromArrays() {
        double[] P = new double[]{0,0,0,0,0.1,0,0,1,1,1,1,1,0,0,1,1,0,0,0,0,0,0,0,1,1,1,1,1,1,1};
        double[] LW = new double[]{0,0,0,0,0.1,0,20,1,1,1,1,1,0,0,1,1,0,0,0,0,0,0,0,1,1,1,1,1,1,1};
        double[] T = new double[]{15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15};

        Date startDate = TestDataFactory.dateOf(2025, 3, 20);
        List<WeatherObservation> rr = TestDataFactory.buildDailyPrecipitation(P, startDate);
        List<WeatherObservation> bt = TestDataFactory.buildDailyLeafWetness(LW, startDate);
        List<WeatherObservation> tm = TestDataFactory.buildDailyTemperature(T, startDate);

        assertEquals(P.length, rr.size());
        assertEquals(LW.length, bt.size());
        assertEquals(T.length, tm.size());
    }

    /**
     * Test of getProportionMatureAscospores method, of class ModelCalculations.
     */
    @Test
    public void testGetProportionMatureAscospores() {
        System.out.println("getProportionMatureAscospores");
        Double[] DD = {15.0, 30.0, 45.0, 60.0, 75.0, 90.0, 105.0, 120.0, 135.0, 150.0};
        Double[] r_D = {0.000446656639695, 0.001591559586838,0.004600086778960,0.011162926962507,0.023408155125329,0.043449120056891,0.072836000421514, 0.112137682859108,0.160799909341000,0.217287214225434};     
        ModelCalculations instance = new ModelCalculations();
        for(int i=0;i<DD.length;i++)
        {
            // Allow small floating-point rounding differences
            assertEquals(r_D[i], instance.getProportionMatureAscospores(DD[i]), 1e-4);
        }
    }

    /**
     * Test of getProportionMatureConidia method, of class ModelCalculations.
     */
    @Test
    public void testGetProportionMatureConidia() {
        
        System.out.println("getProportionMatureConidia");
        Double[] DD = {15.0, 30.0, 45.0, 60.0, 60.0, 60.0, 75.0, 90.0, 105.0, 120.0};
        Double[] r_D = {0.1672, 0.2295, 0.2979, 0.3692, 0.3692, 0.3692, 0.4405, 0.5094, 0.5740, 0.6333};
        ModelCalculations instance = new ModelCalculations();
        for(int i=0;i<DD.length;i++)
        {
            // Allow small floating-point rounding differences
            assertEquals(r_D[i], instance.getProportionMatureConidia(DD[i]), 1e-4);
        }
    }

    /**
     * Test of calculateSporeMaturation method, of class ModelCalculations.
     * The method does three things (for both conidia and ascospores)
     * 1) Calculates consecutive dry days
     * 2) Calculates DD sum 
     * 3) Calculates spore maturation 
     */
    @org.junit.jupiter.api.Test
    public void testCalculateSporeMaturation() throws Exception {
        System.out.println("calculateSporeMaturation");
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Oslo");
        ModelCalculations instance = new ModelCalculations();
        List<WeatherObservation> allObservations = instance.validateAndInitializeWeatherData(new WeatherDataFileReader().getWeatherObservationsFromFile("/hourly_all_dry.json"), timeZone).get(WeatherObservation.LOG_INTERVAL_ID_1D);
        Collections.sort(allObservations);
        //allObservations.forEach(o->System.out.println(o.toString()));
        DateMap dataMatrix = instance.initDataMatrix(allObservations);
        //System.out.println(dataMatrix.toCSV());
        ModelId modelId = MycosphaerellaRibisModel.MODEL_ID;

        
        
        //System.out.println(dataMatrix.toCSV());
        // Test for consecutive dry days
        int[] cdd = {1,0,1,2,3,4,5,6,0,0,0,1,2,0,1,2,0,1,2,3,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,0,1};
        double[] ddsum_c = {20.2,42,64.9,81.8,92.6,92.6,92.6,92.6,112.8,132.6,146.6,161.7,174.8,188.4,202.7,217.2,228.7,242,256.3,267.7,277.2,287.7,302.6,318.8,318.8,318.8,318.8,318.8,318.8,318.8,318.8,318.8,318.8,318.8,318.8,318.8,318.8,318.8,334.4,354.8};
        double[] ddsum_a = {20.2,42,64.9,81.8,92.6,104.7,123,141.4,161.6,181.4,195.4,210.5,223.6,237.2,251.5,266,277.5,290.8,305.1,316.5,326,336.5,351.4,367.6,388.2,409.7,431.2,431.2,431.2,431.2,431.2,431.2,431.2,431.2,431.2,431.2,431.2,431.2,446.8,467.2};
        double[] r_d_c = {0.187945002913553,0.283913862997721,0.392624947535078,0.47213028427304,0.520902345227088,0.520902345227088,0.520902345227088,0.520902345227088,0.605580101618331,0.678587425667368,0.723812994555606,0.766735858771659,0.799298302255655,0.828848530671433,0.855668055353501,0.878891432452475,0.89478866177124,0.91072280583163,0.92528617242865,0.935236077127746,0.942539528517805,0.949683264629719,0.958356401001937,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.966128953967331,0.972259135451774,0.978651750380572};
        double[] r_d_a = {0.000712240885033828,0.00377652822391743,0.014429758835193,0.0314154070756603,0.0478431645192446,0.0721511329249885,0.121157996523407,0.184060705385334,0.264962205011599,0.350889300149704,0.412579667236057,0.477782557979756,0.531975529056105,0.58501437389311,0.636620532016749,0.684229053688444,0.718528918887265,0.754433420964183,0.788711436409653,0.813013406255088,0.831350120000637,0.849728826269786,0.872688107559728,0.893938365141164,0.916161363598242,0.934586982156168,0.949075891403917,0.949075891403917,0.949075891403917,0.949075891403917,0.949075891403917,0.949075891403917,0.949075891403917,0.949075891403917,0.949075891403917,0.949075891403917,0.949075891403917,0.949075891403917,0.957582439581746,0.966637319975743};
        
        // Set start date for spore maturation one day off into future to make it fail
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2018, Calendar.JUNE, 2, 0, 0, 0);
        cal.set(Calendar.MILLISECOND,0);

        instance.calculateSporeMaturation(dataMatrix, timeZone, cal.getTime());
        
        // The first date we set is one day off from expected results, so should fail 
        Boolean failed = false;
        try {
            assertEquals(ddsum_c[0], dataMatrix.getParamDoubleValueForDate(
                dataMatrix.getFirstDateWithParameterValue(DataMatrix.CONSECUTIVE_DRY_DAYS)
            , DataMatrix.DD_SUM_CONIDIA),0.5);
        }
        catch(AssertionFailedError afe)
        {
            failed = true;
        }

        assertTrue(failed);

        // Same thing, but two days before we have weather data
        failed = false;
        cal.set(2018, Calendar.MAY, 29, 0, 0, 0);
        cal.set(Calendar.MILLISECOND,0);
        try {
            instance.calculateSporeMaturation(dataMatrix, timeZone, cal.getTime());
        
            assertEquals(ddsum_c[0], dataMatrix.getParamDoubleValueForDate(
                dataMatrix.getFirstDateWithParameterValue(DataMatrix.CONSECUTIVE_DRY_DAYS)
            , DataMatrix.DD_SUM_CONIDIA),0.5);
        }
        catch(AssertionFailedError | NullPointerException ex)
        {
            failed = true;
        }

        assertTrue(failed);

        // Set the correct start date and test the entire dataset
        cal.set(2018, Calendar.JUNE, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND,0);
        instance.calculateSporeMaturation(dataMatrix, timeZone, cal.getTime());
        Date startCDD = dataMatrix.getFirstDateWithParameterValue(DataMatrix.CONSECUTIVE_DRY_DAYS);
        Date endCDD = dataMatrix.getLastDateWithParameterValue(DataMatrix.CONSECUTIVE_DRY_DAYS);
        Date currentDate = startCDD;
        int index = 0;
        while(currentDate.compareTo(endCDD) <= 0)
        {
            assertEquals(cdd[index], dataMatrix.getParamIntValueForDate(currentDate, DataMatrix.CONSECUTIVE_DRY_DAYS));
            assertEquals(ddsum_c[index], dataMatrix.getParamDoubleValueForDate(currentDate, DataMatrix.DD_SUM_CONIDIA),0.5);
            assertEquals(ddsum_a[index], dataMatrix.getParamDoubleValueForDate(currentDate, DataMatrix.DD_SUM_ASCOSPORES),0.5);
            assertEquals(r_d_c[index], dataMatrix.getParamDoubleValueForDate(currentDate, DataMatrix.PROPORTION_MATURE_CONIDIA),0.1);
            assertEquals(r_d_a[index], dataMatrix.getParamDoubleValueForDate(currentDate, DataMatrix.PROPORTION_MATURE_ASCOSPORES),0.1);
            cal.add(Calendar.DATE, 1);
            currentDate = cal.getTime();
            index++;
        }

        //System.out.println(dataMatrix.toCSV());

    }

    /**
     * Test of accumulateMills method, of class ModelCalculations.
     */
    @Test
    public void testAccumulateMills() throws Exception {
        System.out.println("accumulateMills");
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Oslo");
        ModelCalculations instance = new ModelCalculations();
        Map<Integer,List<WeatherObservation>> allObservations = instance.validateAndInitializeWeatherData(new WeatherDataFileReader().getWeatherObservationsFromFile("/hourly_all_dry.json"), timeZone);

        //allObservations.forEach(o->System.out.println(o.toString()));
        DataMatrix dailyData = instance.initDataMatrix(allObservations.get(WeatherObservation.LOG_INTERVAL_ID_1D));
        DataMatrix hourlyData = instance.initDataMatrix(allObservations.get(WeatherObservation.LOG_INTERVAL_ID_1H));
        //System.out.println(hourlyData.toCSV());
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2018, Calendar.JUNE, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND,0);
        Date startDateSporeMaturation = cal.getTime();
        
        DataMatrix expResult = null;
        instance.calculateSporeMaturation(dailyData, timeZone, startDateSporeMaturation);
        DataMatrix result = instance.accumulateMills(hourlyData, dailyData, startDateSporeMaturation, timeZone);
        // Assume that we have at least one incident of yellow and one of red warning
        Date currentDate = startDateSporeMaturation;
        Date endMills = hourlyData.getLastDateWithParameterValue(DataMatrix.ACCUMULATED_MILLS);
        cal.setTime(currentDate);
        Boolean yellowWarningIssued = false;
        Boolean redWarningIssued = false;
        while(currentDate.compareTo(endMills) <= 0)
        {
            Double accumulatedMills = result.getParamDoubleValueForDate(currentDate, DataMatrix.ACCUMULATED_MILLS);
            if(accumulatedMills >= MycosphaerellaRibisModel.MILLS_THRESHOLD_YELLOW_WARNING && accumulatedMills < MycosphaerellaRibisModel.MILLS_THRESHOLD_RED_WARNING)
            {
                yellowWarningIssued = true;
            }
            else if(accumulatedMills >= MycosphaerellaRibisModel.MILLS_THRESHOLD_RED_WARNING)
            {
                redWarningIssued = true;
            }
            cal.add(Calendar.HOUR, 1);
            currentDate = cal.getTime();
        }

        //System.out.println(result.toCSV());
        assumeTrue(yellowWarningIssued);
        assumeTrue(redWarningIssued);
    }
    
}
