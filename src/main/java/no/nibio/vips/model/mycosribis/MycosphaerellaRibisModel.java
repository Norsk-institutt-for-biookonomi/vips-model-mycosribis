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

import java.util.List;

import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.i18n.I18nImpl;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.model.Model;
import no.nibio.vips.model.ModelExcecutionException;
import no.nibio.vips.model.ModelId;


/**
 *
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MycosphaerellaRibisModel extends I18nImpl implements Model {


    public MycosphaerellaRibisModel()
    {
        super("no.nibio.vips.model.MycosphaerellaRibisModel.texts");
    }

    @Override
    public List<Result> getResult() throws ModelExcecutionException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getResult'");
    }

    @Override
    public void setConfiguration(ModelConfiguration arg0) throws ConfigValidationException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setConfiguration'");
    }

    @Override
    public String getCopyright() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCopyright'");
    }

    @Override
    public String getLicense() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLicense'");
    }

    @Override
    public String getModelDescription() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelDescription'");
    }

    @Override
    public String getModelDescription(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelDescription'");
    }

    @Override
    public ModelId getModelId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelId'");
    }

    @Override
    public String getModelName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelName'");
    }

    @Override
    public String getModelName(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelName'");
    }

    @Override
    public String getModelUsage() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelUsage'");
    }

    @Override
    public String getModelUsage(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelUsage'");
    }

   

    @Override
    public String getSampleConfig() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSampleConfig'");
    }

    @Override
    public String getWarningStatusInterpretation() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWarningStatusInterpretation'");
    }

    @Override
    public String getWarningStatusInterpretation(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWarningStatusInterpretation'");
    }

}
