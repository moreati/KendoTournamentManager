package com.softwaremagico.kt.core.managers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
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

import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleGroupFightManager {
    private final FightProvider fightProvider;
    private final DuelProvider duelProvider;

    @Autowired
    public SimpleGroupFightManager(FightProvider fightProvider, DuelProvider duelProvider) {
        this.fightProvider = fightProvider;
        this.duelProvider = duelProvider;
    }


    public List<Fight> createFights(Tournament tournament, List<Team> teams, TeamsOrder teamsOrder, Integer level, String createdBy) {
        return createCompleteFightList(tournament, teams, teamsOrder, level, createdBy);
    }

    private Fight createFight(Tournament tournament, Team team1, Team team2, Integer shiaijo, Integer level, String createdBy) {
        return new Fight(tournament, team1, team2, shiaijo, level, createdBy);
    }

    /**
     * Create a list of fights where all teams fight versus all others.
     *
     * @param tournament
     * @param teams
     * @param teamsOrder
     * @return
     */
    protected List<Fight> createCompleteFightList(Tournament tournament, List<Team> teams, TeamsOrder teamsOrder, Integer level,
                                                  String createdBy) {
        if (teams == null || tournament == null || teams.size() < 2) {
            return null;
        }
        final List<Fight> fights = new ArrayList<>();
        final TeamSelector teamSelector = new TeamSelector(teams, teamsOrder);

        Team team1 = teamSelector.getTeamWithMoreAdversaries(teamsOrder);
        Fight fight, lastFight = null;
        while (teamSelector.remainFights()) {
            final Team team2 = teamSelector.getNextAdversary(team1, teamsOrder);
            // Team1 has no more adversaries. Use another one.
            if (team2 == null) {
                team1 = teamSelector.getTeamWithMoreAdversaries(teamsOrder);
                continue;
            }
            // Remaining fights sometimes repeat team. Align them.
            if (lastFight != null && (lastFight.getTeam1().equals(team2) || lastFight.getTeam2().equals(team1))) {
                fight = createFight(tournament, team2, team1, 0, level, createdBy);
            } else if (lastFight != null && (lastFight.getTeam1().equals(team1) || lastFight.getTeam2().equals(team2))) {
                fight = createFight(tournament, team1, team2, 0, level, createdBy);
            } else if (fights.size() % 2 == 0) {
                fight = createFight(tournament, team1, team2, 0, level, createdBy);
            } else {
                fight = createFight(tournament, team2, team1, 0, level, createdBy);
            }
            fights.add(fight);
            lastFight = fight;
            teamSelector.removeAdversary(team1, team2);
            team1 = team2;
        }
        return fights;
    }
}