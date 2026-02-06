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
    public final static String CONSECUTIVE_DRY_DAYS = "CDD"; // Consecutive Dry Days
    public final static String DD_SUM_ASCOSPORES = "DSA"; // Day degree sum for ascospores
    public final static String DD_SUM_CONIDIOSPORES = "DSC"; // Day degree sum for conidiospores
    public final static String DAILY_LW = "DBT"; // Daily leaf wetness duration in hours
    
}
