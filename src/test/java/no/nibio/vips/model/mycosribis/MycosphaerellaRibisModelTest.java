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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.model.ModelId;
import no.nibio.vips.util.test.WeatherDataFileReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MycosphaerellaRibisModelTest {
    
    public MycosphaerellaRibisModelTest() {
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
     * Test of getResult method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetResult() throws Exception {
        System.out.println("getResult");
        ModelConfiguration config = new ModelConfiguration();
        config.setModelId(MycosphaerellaRibisModel.MODEL_ID.toString());
        config.setConfigParameter("observations", new WeatherDataFileReader().getWeatherObservationsFromFile("/hourly_all_dry.json"));
        config.setConfigParameter("timeZone", "Europe/Oslo");
        config.setConfigParameter("startDateSporeMaturation", "2018-06-01");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        instance.setConfiguration(config);
        List<Result> results = instance.getResult();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
        cal.set(2018, Calendar.JUNE, 11, 5, 0, 0);
        cal.set(Calendar.MILLISECOND,0);
        Date yellowTime = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY,17);
        Date redTime = cal.getTime();
        for(Result result:results)
        {
            if(result.getValidTimeStart().compareTo(yellowTime) == 0)
            {
                assertEquals(98, Double.valueOf(result.getValue(MycosphaerellaRibisModel.MODEL_ID.toString(), DataMatrix.ACCUMULATED_MILLS)).intValue());
                assertEquals(3, result.getWarningStatus());
            }
            if(result.getValidTimeStart().compareTo(redTime) == 0)
            {
                assertEquals(198, Double.valueOf(result.getValue(MycosphaerellaRibisModel.MODEL_ID.toString(), DataMatrix.ACCUMULATED_MILLS)).intValue());
                assertEquals(4, result.getWarningStatus());
            }
        }
    }

    /**
     * Test of setConfiguration method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testSetConfiguration() throws Exception {
        System.out.println("setConfiguration");
        ModelConfiguration config = new ModelConfiguration();
        config.setModelId(MycosphaerellaRibisModel.MODEL_ID.toString());
        config.setConfigParameter("observations", new WeatherDataFileReader().getWeatherObservationsFromFile("/hourly_all_dry.json"));
        config.setConfigParameter("timeZone", "Europe/Oslo");
        config.setConfigParameter("startDateSporeMaturation", "2018-06-01");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        instance.setConfiguration(config);
    }

    /**
     * Test of getCopyright method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetCopyright() {
        System.out.println("getCopyright");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        String expResult = "(c) 2026 NIBIO";
        String result = instance.getCopyright();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLicense method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetLicense() {
        System.out.println("getLicense");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();        
        String result = instance.getLicense();
        assertNotNull(result);
        assertNotEquals(result.trim().length(), 0);

    }

    /**
     * Test of getModelDescription method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetModelDescription_0args() {
        System.out.println("getModelDescription");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        String result = instance.getModelDescription();
        assertNotNull(result);
        assertNotEquals(result.trim().length(), 0);
    }

    /**
     * Test of getModelDescription method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetModelDescription_String() {
        System.out.println("getModelDescription");
        String arg0 = "";
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        String result = instance.getModelDescription("nb");
        assertNotNull(result);
        assertNotEquals(result.trim().length(), 0);
    }

    /**
     * Test of getModelId method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetModelId() {
        System.out.println("getModelId");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        ModelId expResult = MycosphaerellaRibisModel.MODEL_ID;
        ModelId result = instance.getModelId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getModelName method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetModelName_0args() {
        System.out.println("getModelName");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        String expResult = "Mycosphaerella ribis model";
        String result = instance.getModelName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getModelName method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetModelName_String() {
        System.out.println("getModelName");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        String expResult = "Bærbuskbladflekkmodell";
        String result = instance.getModelName("nb");
        assertEquals(expResult, result);
    }

    /**
     * Test of getModelUsage method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetModelUsage_0args() {
        System.out.println("getModelUsage");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        String result = instance.getModelUsage();
        assertNotNull(result);
        assertNotEquals(result.trim().length(), 0);
    }

    /**
     * Test of getModelUsage method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetModelUsage_String() {
        System.out.println("getModelUsage");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        String result = instance.getModelUsage("nb");
        assertNotNull(result);
        assertNotEquals(result.trim().length(), 0);
    }

    /**
     * Test of getSampleConfig method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetSampleConfig() {
        System.out.println("getSampleConfig");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        String expResult = "{\n" +
                "\t\"modelId\":\"" + MycosphaerellaRibisModel.MODEL_ID.toString() + "\",\n" +
                "\t\"configParameters\":{\n" +
                "\t\t\"timeZone\":\"Europe/Oslo\",\n" +
                "\t\t\"startSporeMaturation\":\"2026-04-25\",\n" +
                "\t\t\"observations\":[\n" +
                "\t\t{\n" +
                "\t\t\t\t\"timeMeasured\": \"2026-04-25T00:00:00+02:00\",\n" +
                "\t\t\t\t\"elementMeasurementTypeId\":\"TM\",\n" +
                "\t\t\t\t\"logIntervalId\":1,\n" +
                "\t\t\t\t\"value\":2.937\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\t\"timeMeasured\": \"2026-04-25T00:00:00+02:00\",\n" +
                "\t\t\t\t\"elementMeasurementTypeId\":\"RR\",\n" +
                "\t\t\t\t\"logIntervalId\":1,\n" +
                "\t\t\t\t\"value\":0\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\t\"timeMeasured\": \"2026-04-25T00:00:00+02:00\",\n" +
                "\t\t\t\t\"elementMeasurementTypeId\":\"BT\",\n" +
                "\t\t\t\t\"logIntervalId\":1,\n" +
                "\t\t\t\t\"value\":0\n" +
                "\t\t}\n" +
                "\t\t]\n" +
                "\t}\n" +
                "}\n";
        String result = instance.getSampleConfig();
        assertEquals(expResult, result);
    }

    /**
     * Test of getWarningStatusInterpretation method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetWarningStatusInterpretation_0args() {
        System.out.println("getWarningStatusInterpretation");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        String result = instance.getWarningStatusInterpretation();
        assertNotNull(result);
        assertNotEquals(result.trim().length(), 0);
    }

    /**
     * Test of getWarningStatusInterpretation method, of class MycosphaerellaRibisModel.
     */
    @Test
    public void testGetWarningStatusInterpretation_String() {
        System.out.println("getWarningStatusInterpretation");
        MycosphaerellaRibisModel instance = new MycosphaerellaRibisModel();
        String result = instance.getWarningStatusInterpretation("nb");
        assertNotNull(result);
        assertNotEquals(result.trim().length(), 0);
    }
    
}
