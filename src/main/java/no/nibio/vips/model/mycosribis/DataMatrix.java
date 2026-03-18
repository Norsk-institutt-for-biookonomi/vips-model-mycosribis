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

import no.nibio.vips.util.DateMap;

public class DataMatrix extends DateMap {
    public final static String CONSECUTIVE_DRY_DAYS = "CONSECUTIVE_DRY_DAYS"; // Consecutive Dry Days
    public final static String DD_SUM_ASCOSPORES = "DD_SUM_ASCOSPORES"; // Day degree sum for ascospores
    public final static String DD_SUM_CONIDIA = "DD_SUM_CONIDIA"; // Day degree sum for conidiospores
    public final static String PROPORTION_MATURE_ASCOSPORES = "PROPORTION_MATURE_ASCOSPORES";
    public final static String PROPORTION_MATURE_CONIDIA = "PROPORTION_MATURE_CONIDIA";
    public final static String DAILY_LW_HOURS = "DAILY_BT_HOURS"; // Daily leaf wetness duration in hours
    public final static String ACCUMULATED_RISK = "ACCRISK";
    public final static String RISK_PERIOD_DURATION_HOURS = "RISK_PERIOD_DURATION_HOURS";
    public final static String ACCUMULATED_RAIN_LAST_3_HOURS = "ACCRR";
    public final static String RAIN_DURATION_HOURS = "RAIN_DURATION_HOURS";
    public final static String IS_SPORE_CASTING_EVENT = "IS_SPORE_CASTING_EVENT";
    public final static String ACCUMULATED_ASCOSPORES = "ACCUMULATED_ASCOSPORES";

    
}
