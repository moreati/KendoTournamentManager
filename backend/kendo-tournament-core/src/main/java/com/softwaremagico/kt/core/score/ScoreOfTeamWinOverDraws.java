package com.softwaremagico.kt.core.score;

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


import java.util.Comparator;
import java.util.Objects;

public class ScoreOfTeamWinOverDraws implements Comparator<ScoreOfTeam> {

    private final boolean checkLevel;

    public ScoreOfTeamWinOverDraws(boolean checkLevel) {
        this.checkLevel = checkLevel;
    }

    @Override
    public int compare(ScoreOfTeam scoreOfTeam1, ScoreOfTeam scoreOfTeam2) {
        if (checkLevel) {
            if (!Objects.equals(scoreOfTeam1.getLevel(), scoreOfTeam2.getLevel())) {
                return scoreOfTeam2.getLevel().compareTo(scoreOfTeam1.getLevel());
            }
        }

        if (!Objects.equals(scoreOfTeam1.getWonFights(), scoreOfTeam2.getWonFights())) {
            return scoreOfTeam2.getWonFights().compareTo(scoreOfTeam1.getWonFights());
        }

        if (!Objects.equals(scoreOfTeam1.getWonDuels(), scoreOfTeam2.getWonDuels())) {
            return scoreOfTeam2.getWonDuels().compareTo(scoreOfTeam1.getWonDuels());
        }

        if (!Objects.equals(scoreOfTeam1.getHits(), scoreOfTeam2.getHits())) {
            return scoreOfTeam2.getHits().compareTo(scoreOfTeam1.getHits());
        }

        if (!Objects.equals(scoreOfTeam1.getDrawFights(), scoreOfTeam2.getDrawFights())) {
            return scoreOfTeam2.getDrawFights().compareTo(scoreOfTeam1.getDrawFights());
        }

        if (!Objects.equals(scoreOfTeam1.getDrawDuels(), scoreOfTeam2.getDrawDuels())) {
            return scoreOfTeam2.getDrawDuels().compareTo(scoreOfTeam1.getDrawDuels());
        }

        return scoreOfTeam2.getUntieDuels().compareTo(scoreOfTeam1.getUntieDuels());
    }
}
