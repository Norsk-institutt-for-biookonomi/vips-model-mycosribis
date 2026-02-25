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

import java.util.Objects;

/**
 * Composite key for the map in {@see MillsTable}
 * @copyright 2026 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MillsTableKey {
    private Double temperature;
    private Integer appleScabStadium;

    public MillsTableKey(Double temperature,Integer appleScabStadium)
    {
        this.temperature = temperature;
        this.appleScabStadium = appleScabStadium;
    }


    @Override
    public int hashCode()
    {
        return this.temperature.intValue() * 100 + this.appleScabStadium;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MillsTableKey other = (MillsTableKey) obj;
        if (!Objects.equals(this.temperature, other.temperature)) {
            return false;
        }
        if (!Objects.equals(this.appleScabStadium, other.appleScabStadium)) {
            return false;
        }
        return true;
    }
}
