package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.models.TournamentStatisticsDTO;
import com.softwaremagico.kt.core.converters.models.FightStatisticsConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentStatisticsConverterRequest;
import com.softwaremagico.kt.core.statistics.TournamentStatistics;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class TournamentStatisticsConverter extends ElementConverter<TournamentStatistics, TournamentStatisticsDTO, TournamentStatisticsConverterRequest> {

    private final FightStatisticsConverter fightStatisticsConverter;

    public TournamentStatisticsConverter(FightStatisticsConverter fightStatisticsConverter) {
        this.fightStatisticsConverter = fightStatisticsConverter;
    }

    @Override
    protected TournamentStatisticsDTO convertElement(TournamentStatisticsConverterRequest from) {
        if (from == null) {
            return null;
        }
        final TournamentStatisticsDTO tournamentStatisticsDTO = new TournamentStatisticsDTO();
        BeanUtils.copyProperties(from.getEntity(), tournamentStatisticsDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        tournamentStatisticsDTO.setFightStatistics(fightStatisticsConverter.convertElement(
                new FightStatisticsConverterRequest(from.getEntity().getFightStatistics())));
        return tournamentStatisticsDTO;
    }

    @Override
    public TournamentStatistics reverse(TournamentStatisticsDTO to) {
        if (to == null) {
            return null;
        }
        final TournamentStatistics tournamentStatistics = new TournamentStatistics();
        BeanUtils.copyProperties(to, tournamentStatistics, ConverterUtils.getNullPropertyNames(to));
        tournamentStatistics.setFightStatistics(fightStatisticsConverter.reverse(to.getFightStatistics()));
        return tournamentStatistics;
    }
}
