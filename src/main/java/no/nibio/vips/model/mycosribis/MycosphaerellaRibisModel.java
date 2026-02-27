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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Calendar;

import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.entity.ResultImpl;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.i18n.I18nImpl;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.model.Model;
import no.nibio.vips.model.ModelExcecutionException;
import no.nibio.vips.model.ModelId;
import no.nibio.vips.util.CommonNamespaces;
import no.nibio.vips.util.ModelUtil;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;


/**
 *
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MycosphaerellaRibisModel extends I18nImpl implements Model {

    public final static ModelId MODEL_ID = new ModelId("MYCOSRIBIS");
    private ModelUtil modelUtil;
    private WeatherUtil weatherUtil;
    private ModelCalculations modelCalculations;
    private TimeZone timeZone;
    private DataMatrix dailyDataMatrix;
    private DataMatrix hourlyDataMatrix;
    private Date startDateSporeMaturation;

    // Thresholds for warning status
    public final static Double MILLS_THRESHOLD_RED_WARNING = 100d;
    public final static Double MILLS_THRESHOLD_YELLOW_WARNING = 90d;

    public MycosphaerellaRibisModel()
    {
        super("no.nibio.vips.model.mycosribis.texts");
        this.modelUtil = new ModelUtil();
        this.weatherUtil = new WeatherUtil();
        this.modelCalculations = new ModelCalculations();
    }

    @Override
    public List<Result> getResult() throws ModelExcecutionException {
        if(this.hourlyDataMatrix == null || this.dailyDataMatrix == null)
        {
            throw new ModelExcecutionException("ERROR: The model has not been initialized with input data. Aborting.");
        }
        
        try
        {
            // Calculate model background data
            this.modelCalculations.calculateSporeMaturation(this.dailyDataMatrix, this.timeZone, this.startDateSporeMaturation);
            this.hourlyDataMatrix = this.modelCalculations.accumulateMills(hourlyDataMatrix, dailyDataMatrix, startDateSporeMaturation, timeZone);

            // Create results!
            List<Result> results = new ArrayList<>();
            Double ascosporeMaturity = 0d;
            Double conidiaMaturity = 0d;
            for(Date currentDate : this.hourlyDataMatrix.getSortedDateKeys())
            {
                if(currentDate.before(startDateSporeMaturation))
                {
                    continue;
                }

                
                Result result = new ResultImpl();
                result.setValidTimeStart(currentDate);

                // Set infection threshold values - to help display it in charts
                result.setValue(CommonNamespaces.NS_FORECAST, "THRESHOLD_LOW", MILLS_THRESHOLD_YELLOW_WARNING.toString());
                result.setValue(CommonNamespaces.NS_FORECAST, "THRESHOLD_HIGH", MILLS_THRESHOLD_RED_WARNING.toString());

                // Set model background values
                Double accumulatedMills = this.hourlyDataMatrix.getParamDoubleValueForDate(currentDate, DataMatrix.ACCUMULATED_MILLS);
                result.setValue(this.getModelId().toString(), DataMatrix.ACCUMULATED_MILLS, String.valueOf(accumulatedMills));

                Date midnight = this.weatherUtil.normalizeToExactDate(currentDate, this.timeZone);
                if(midnight.compareTo(currentDate) == 0)
                {
                    ascosporeMaturity = this.dailyDataMatrix.getParamDoubleValueForDate(midnight, DataMatrix.PROPORTION_MATURE_ASCOSPORES);
                    result.setValue(this.getModelId().toString(), DataMatrix.PROPORTION_MATURE_ASCOSPORES, ascosporeMaturity != null ? String.valueOf(ascosporeMaturity) : "");
                    conidiaMaturity = this.dailyDataMatrix.getParamDoubleValueForDate(midnight, DataMatrix.PROPORTION_MATURE_CONIDIA);
                    result.setValue(this.getModelId().toString(), DataMatrix.PROPORTION_MATURE_CONIDIA, conidiaMaturity != null ? String.valueOf(conidiaMaturity) : "");
                }
                
                

                // Determine the warning status based on accumulated Mills
                Integer warningStatus = null;
                if(accumulatedMills == null || ascosporeMaturity == 100d)
                {
                    warningStatus = Result.WARNING_STATUS_NO_WARNING;
                }
                else 
                {
                    if(accumulatedMills >= MILLS_THRESHOLD_RED_WARNING)
                    {
                        warningStatus = Result.WARNING_STATUS_HIGH_RISK;
                    }
                    else if(accumulatedMills >= MILLS_THRESHOLD_YELLOW_WARNING)
                    {
                        warningStatus = Result.WARNING_STATUS_MINOR_RISK;
                    }
                    else
                    {
                        warningStatus = Result.WARNING_STATUS_NO_RISK;
                    }
                }
                result.setWarningStatus(warningStatus);

                results.add(result);
            }
            return results;
        }
        catch(WeatherObservationListException ex)
        {
            throw new ModelExcecutionException(ex.getMessage());
        }

    }

    /**
     * 
     */
    @Override
    public void setConfiguration(ModelConfiguration config) throws ConfigValidationException {
        try
        {
            // Required parameters: timeZone, startDateSporeMaturation and observations
            this.timeZone = TimeZone.getTimeZone((String) config.getConfigParameter("timeZone"));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            this.startDateSporeMaturation = format.parse(config.getConfigParameter("startDateSporeMaturation").toString());
            format.setTimeZone(timeZone);
            this.dailyDataMatrix = modelCalculations.initDataMatrix(
                this.modelCalculations.validateAndInitializeWeatherData(
                    this.modelUtil.extractWeatherObservationList(config.getConfigParameter("observations")),timeZone).get(WeatherObservation.LOG_INTERVAL_ID_1D)
                );  
            this.hourlyDataMatrix = modelCalculations.initDataMatrix(
                this.modelCalculations.validateAndInitializeWeatherData(
                    this.modelUtil.extractWeatherObservationList(config.getConfigParameter("observations")),timeZone).get(WeatherObservation.LOG_INTERVAL_ID_1H)
                ); 
        }
        catch(NullPointerException npe)
        {
            throw new ConfigValidationException("Missing one or more of these parameters: "
                    + "timeZone");
        }
        catch(ParseException pe)
        {
            throw new ConfigValidationException("Illegal date format for startDateMaturation: " + config.getConfigParameter("startDateSporeMaturation") + ". Correct format is yyyy-MM-dd");
        }
    }

    @Override
    public String getCopyright() {
        return "(c) 2026 NIBIO";
    }

    @Override
    public String getLicense() {
        return 
                "  Copyright (c) 2026 NIBIO <http://www.nibio.no/>. \n" +
                "  \n" +
                "  This program is free software: you can redistribute it and/or modify\n" +
                "  it under the terms of the GNU Affero General Public License as published by\n" +
                "  the Free Software Foundation, either version 3 of the License, or\n" +
                "  (at your option) any later version.\n" +
                " \n" +
                "  This program is distributed in the hope that it will be useful,\n" +
                "  but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                "  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                "  GNU Affero General Public License for more details.\n" +
                " \n" +
                "  You should have received a copy of the GNU Affero General Public License\n" +
                "  along with this program.  If not, see <https://www.gnu.org/licenses/>.\n" +
                "  \n" ;
    }

    @Override
    public String getModelDescription() {
        return this.getModelDescription(Model.DEFAULT_LANGUAGE);
    }

    @Override
    public String getModelDescription(String language) {
        return this.getText("description", language);
    }

    @Override
    public ModelId getModelId() {
        return MycosphaerellaRibisModel.MODEL_ID;
    }

    @Override
    public String getModelName() {
        return this.getModelName(Model.DEFAULT_LANGUAGE);
    }

    @Override
    public String getModelName(String language) {
       return this.getText("name", language);
    }

    @Override
    public String getModelUsage() {
        return this.getModelUsage(Model.DEFAULT_LANGUAGE);
    }

    @Override
    public String getModelUsage(String language) {
        return this.getText("usage", language);
    }

    @Override
    public String getSampleConfig() {
        return  "{\n" +
                "\t\"modelId\":\"" + MODEL_ID.toString() + "\",\n" +
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
    }

    @Override
    public String getWarningStatusInterpretation() {
        return this.getWarningStatusInterpretation(Model.DEFAULT_LANGUAGE);
    }

    @Override
    public String getWarningStatusInterpretation(String language) {
        return this.getText("statusInterpretation", language);
    }

}
