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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @copyright 2026 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MillsTable {
    
    private static final Map<MillsTableKey, Double> MillsTable;
    
    public Double getMillsValue(Double temperature, Integer appleScabStadium)
    {
        Double millsValue = MillsTable.get(new MillsTableKey(Math.floor(temperature), appleScabStadium));
        return millsValue != null ? millsValue : 0d;
    }
    
    static {
        Map<MillsTableKey, Double> aMap = new HashMap<>();
        /**
         * These were generated from NIBIO's VIPS database from this query:
         * SELECT 'aMap.put(new MillsTableKey(' || temperatur ||  'd,' || type ||  '),' || mills_verdi ||  'd);' FROM mills_tabell;
         */
        aMap.put(new MillsTableKey(0d,1),2d);
        aMap.put(new MillsTableKey(1d,1),2d);
        aMap.put(new MillsTableKey(2d,1),3d);
        aMap.put(new MillsTableKey(3d,1),3d);
        aMap.put(new MillsTableKey(4d,1),4d);
        aMap.put(new MillsTableKey(5d,1),5d);
        aMap.put(new MillsTableKey(6d,1),6d);
        aMap.put(new MillsTableKey(7d,1),6d);
        aMap.put(new MillsTableKey(8d,1),7d);
        aMap.put(new MillsTableKey(9d,1),8d);
        aMap.put(new MillsTableKey(10d,1),9d);
        aMap.put(new MillsTableKey(11d,1),11d);
        aMap.put(new MillsTableKey(12d,1),12d);
        aMap.put(new MillsTableKey(13d,1),13d);
        aMap.put(new MillsTableKey(14d,1),14d);
        aMap.put(new MillsTableKey(15d,1),14d);
        aMap.put(new MillsTableKey(16d,1),16d);
        aMap.put(new MillsTableKey(17d,1),17d);
        aMap.put(new MillsTableKey(18d,1),17d);
        aMap.put(new MillsTableKey(19d,1),17d);
        aMap.put(new MillsTableKey(20d,1),17d);
        aMap.put(new MillsTableKey(21d,1),17d);
        aMap.put(new MillsTableKey(22d,1),17d);
        aMap.put(new MillsTableKey(23d,1),17d);
        aMap.put(new MillsTableKey(24d,1),16d);
        aMap.put(new MillsTableKey(25d,1),13d);
        aMap.put(new MillsTableKey(23d,2),12d);
        aMap.put(new MillsTableKey(24d,2),11d);
        aMap.put(new MillsTableKey(25d,2),9d);
        aMap.put(new MillsTableKey(26d,2),7d);
        aMap.put(new MillsTableKey(27d,1),0d);
        aMap.put(new MillsTableKey(28d,1),0d);
        aMap.put(new MillsTableKey(29d,1),0d);
        aMap.put(new MillsTableKey(30d,1),0d);
        aMap.put(new MillsTableKey(31d,1),0d);
        aMap.put(new MillsTableKey(32d,1),0d);
        aMap.put(new MillsTableKey(33d,1),0d);
        aMap.put(new MillsTableKey(27d,2),0d);
        aMap.put(new MillsTableKey(28d,2),0d);
        aMap.put(new MillsTableKey(29d,2),0d);
        aMap.put(new MillsTableKey(30d,2),0d);
        aMap.put(new MillsTableKey(31d,2),0d);
        aMap.put(new MillsTableKey(32d,2),0d);
        aMap.put(new MillsTableKey(33d,2),0d);
        aMap.put(new MillsTableKey(26d,1),9d);
        aMap.put(new MillsTableKey(0d,2),3d);
        aMap.put(new MillsTableKey(1d,2),3d);
        aMap.put(new MillsTableKey(2d,2),3d);
        aMap.put(new MillsTableKey(3d,2),3d);
        aMap.put(new MillsTableKey(4d,2),4d);
        aMap.put(new MillsTableKey(5d,2),4d);
        aMap.put(new MillsTableKey(6d,2),5d);
        aMap.put(new MillsTableKey(7d,2),6d);
        aMap.put(new MillsTableKey(8d,2),7d);
        aMap.put(new MillsTableKey(9d,2),8d);
        aMap.put(new MillsTableKey(10d,2),10d);
        aMap.put(new MillsTableKey(11d,2),11d);
        aMap.put(new MillsTableKey(12d,2),11d);
        aMap.put(new MillsTableKey(13d,2),11d);
        aMap.put(new MillsTableKey(14d,2),11d);
        aMap.put(new MillsTableKey(15d,2),11d);
        aMap.put(new MillsTableKey(16d,2),11d);
        aMap.put(new MillsTableKey(17d,2),11d);
        aMap.put(new MillsTableKey(18d,2),12d);
        aMap.put(new MillsTableKey(19d,2),12d);
        aMap.put(new MillsTableKey(20d,2),13d);
        aMap.put(new MillsTableKey(21d,2),13d);
        aMap.put(new MillsTableKey(22d,2),13d);
       
        MillsTable = Collections.unmodifiableMap(aMap);
    }
    
    

}
