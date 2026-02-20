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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.ibm.icu.impl.locale.LikelySubtags.Data;

import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.i18n.I18nImpl;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.model.Model;
import no.nibio.vips.model.ModelExcecutionException;
import no.nibio.vips.model.ModelId;
import no.nibio.vips.util.ModelUtil;


/**
 *
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MycosphaerellaRibisModel extends I18nImpl implements Model {

    public final static ModelId MODEL_ID = new ModelId("MYCOSRIBIS");
    private ModelUtil modelUtil;
    private ModelCalculations modelCalculations;
    private TimeZone timeZone;
    private DataMatrix dataMatrix;
    private Date startDateSporeMaturation;

    public MycosphaerellaRibisModel()
    {
        super("no.nibio.vips.model.mycosribis.texts");
        this.modelUtil = new ModelUtil();
        this.modelCalculations = new ModelCalculations();
    }

    @Override
    public List<Result> getResult() throws ModelExcecutionException {
        if(this.dataMatrix == null)
        {
            throw new ModelExcecutionException("ERROR: The model has not been initialized with input data. Aborting.");
        }
        this.modelCalculations.calculateSporeMaturation(this.dataMatrix, timeZone, startDateSporeMaturation, MycosphaerellaRibisModel.MODEL_ID);
        // TODO Calculate Mills table infections
        // TODO Create return value (list of results)
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getResult'");
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
            this.dataMatrix = modelCalculations.initDataMatrix(
                this.modelCalculations.validateAndInitializeWeatherData(
                    this.modelUtil.extractWeatherObservationList(config.getConfigParameter("observations")),timeZone)
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
