package com.softwaremagico.kt.utils;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import com.softwaremagico.kt.core.controller.*;
import com.softwaremagico.kt.core.controller.models.*;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicDataTest extends AbstractTestNGSpringContextTests {
    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_CITY = "ClubCity";
    private static final Integer MEMBERS = 3;
    private static final Integer TEAMS = 2;
    private static final String TOURNAMENT_NAME = "basicTournamentTest";

    private static final Integer SHIAIJO = 0;

    private static final Integer LEVEL = 0;

    @Autowired
    private ClubController clubController;

    @Autowired
    private ParticipantController participantController;

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private GroupController groupController;

    @Autowired
    private FightController fightController;

    protected ClubDTO club;

    protected TournamentDTO tournament;
    protected List<ParticipantDTO> members;
    protected List<RoleDTO> roles;
    protected List<TeamDTO> teams;
    protected GroupDTO group;
    protected List<FightDTO> fights;

    protected ClubDTO createClub() {
        return clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null);
    }

    protected List<ParticipantDTO> createParticipants(ClubDTO club) {
        List<ParticipantDTO> members = new ArrayList<>();
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            members.add(participantController.create(new ParticipantDTO(String.format("0000%s", i), String.format("name%s", i), String.format("lastname%s", i), club), null));
        }
        return members;
    }

    protected TournamentDTO createTournament() {
        return tournamentController.create(new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.LEAGUE), null);
    }

    protected List<RoleDTO> createRoles(List<ParticipantDTO> members, TournamentDTO tournament) {
        List<RoleDTO> roles = new ArrayList<>();
        for (ParticipantDTO competitor : members) {
            roles.add(roleController.create(new RoleDTO(tournament, competitor, RoleType.COMPETITOR), null));
        }
        return roles;
    }

    protected RoleDTO createReferee() {
        ParticipantDTO referee = participantController.create(new ParticipantDTO("Ref001", "Referee", "Referee", club), null);
        return roleController.create(new RoleDTO(tournament, referee, RoleType.REFEREE), null);
    }

    protected List<TeamDTO> createTeams(List<ParticipantDTO> members, TournamentDTO tournament) {
        List<TeamDTO> teams = new ArrayList<>();
        int teamIndex = 0;
        TeamDTO team = null;
        int teamMember = 0;
        for (ParticipantDTO competitor : members) {
            // Create a new team.
            if (team == null) {
                teamIndex++;
                team = new TeamDTO("Team" + String.format("%02d", teamIndex), tournament);
                teamMember = 0;
                team = teamController.create(team, null);
                teams.add(team);
            }

            // Add member.
            team.addMember(competitor);
            team = teamController.update(team, null);
            teams.set(teams.size() - 1, team);
            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }
        return teams;
    }

    protected GroupDTO createGroup(TournamentDTO tournament, List<TeamDTO> teams) {
        final GroupDTO group = new GroupDTO();
        group.setTournament(tournament);
        group.setLevel(0);
        group.setTeams(teams);
        return groupController.create(group, null);
    }

    protected List<FightDTO> createFights(TournamentDTO tournament, List<TeamDTO> teams, GroupDTO group) {
        List<FightDTO> fights = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            FightDTO fightDTO = new FightDTO(tournament, teams.get((i) % teams.size()), teams.get((i + 1) % teams.size()), SHIAIJO, LEVEL);
            List<DuelDTO> duels = new ArrayList<>();
            for (int j = 0; j < tournament.getTeamSize(); j++) {
                duels.add(new DuelDTO(teams.get((i) % teams.size()).getMembers().get(j), teams.get((i + 1) % teams.size()).getMembers().get(j),
                        tournament, null));
            }
            fightDTO.setDuels(duels);
            fights.add(fightController.create(fightDTO, null));
        }
        group.setFights(fights);
        groupController.create(group, null);
        return fights;
    }

    protected void resolveFights() {
        int counter = 0;
        for (final FightDTO fight : fights) {
            for (final DuelDTO duel : fight.getDuels()) {
                List<Score> scores = new ArrayList<>();
                for (int i = 0; i < (counter % 3); i++) {
                    scores.add(Score.MEN);
                }
                duel.setCompetitor1Score(scores);
                counter++;
            }
            fightController.update(fight, null);
        }
    }

    protected void populateData() {
        club = createClub();
        members = createParticipants(club);
        tournament = createTournament();
        roles = createRoles(members, tournament);
        createReferee();
        teams = createTeams(members, tournament);
        group = createGroup(tournament, teams);
        fights = createFights(tournament, teams, group);
    }
}
