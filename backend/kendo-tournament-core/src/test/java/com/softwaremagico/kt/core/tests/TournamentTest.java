package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.FightStatisticsController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.tournaments.TreeTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

@SpringBootTest
@Test(groups = {"tournamentTest"})
public class TournamentTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 16;
    private static final int GROUPS = 4;
    private static final String TOURNAMENT_NAME = "TournamentTest";
    private static TournamentDTO tournamentDTO = null;

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private TournamentConverter tournamentConverter;

    @Autowired
    private ParticipantController participantController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private TreeTournamentHandler treeTournamentHandler;

    @Autowired
    private ClubController clubController;

    @Autowired
    private GroupConverter groupConverter;

    @Autowired
    private TeamConverter teamConverter;

    @Autowired
    private RankingProvider rankingProvider;

    @Autowired
    private FightStatisticsController fightStatisticsController;

    @Autowired
    private GroupController groupController;

    @Autowired
    private FightController fightController;

    @Autowired
    private DuelController duelController;

    @Autowired
    private FightConverter fightConverter;

    private ClubDTO clubDTO;


    @Test
    public void addClub() {
        clubDTO = clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null);
    }

    @Test(dependsOnMethods = "addClub")
    public void addParticipants() {
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            participantController.create(new ParticipantDTO(String.format("0000%s", i), String.format("name%s", i), String.format("lastname%s", i), clubDTO), null);
        }
    }

    @Test(dependsOnMethods = "addParticipants")
    public void addTournament() {
        Assert.assertEquals(tournamentController.count(), 0);
        TournamentDTO newTournament = new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.TREE);
        tournamentDTO = tournamentController.create(newTournament, null);
        Assert.assertEquals(tournamentController.count(), 1);
    }

    @Test(dependsOnMethods = {"addTournament"})
    public void addRoles() {
        for (ParticipantDTO competitor : participantController.get()) {
            roleController.create(new RoleDTO(tournamentDTO, competitor, RoleType.COMPETITOR), null);
        }
        Assert.assertEquals(roleController.count(tournamentDTO), participantController.count());
    }

    @Test(dependsOnMethods = "addTournament")
    public void add4Groups() {
        //First group is already inserted.
        for (int i = 1; i < GROUPS; i++) {
            final GroupDTO groupDTO = new GroupDTO();
            groupDTO.setTournament(tournamentDTO);
            groupDTO.setIndex(i);
            groupDTO.setLevel(0);
            groupDTO.setShiaijo(0);
            groupController.create(groupDTO, null);
        }
        Assert.assertEquals(groupController.count(), 7);
    }

    @Test(dependsOnMethods = {"add4Groups"})
    public void addTeams() {
        int teamIndex = 0;
        TeamDTO team = null;
        int teamMember = 0;

        final List<Group> groups = groupController.getGroups(tournamentDTO, 0);

        for (ParticipantDTO competitor : participantController.get()) {
            // Create a new team.
            if (team == null) {
                teamIndex++;
                team = new TeamDTO("Team" + String.format("%02d", teamIndex), tournamentDTO);
                teamMember = 0;
            }

            // Add member.
            team.addMember(competitor);
            team = teamController.create(team, null);

            //Add the new team to a group
            if (teamMember == 0) {
                groupController.addTeams(groups.get((teamIndex - 1) / (TEAMS / GROUPS)).getId(), Collections.singletonList(team), null);
            }

            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }

        Assert.assertEquals(TEAMS, teamController.count(tournamentDTO));

        final List<Group> tournamentGroups = groupController.getGroups(tournamentDTO, 0);
        for (Group group : tournamentGroups) {
            Assert.assertEquals(group.getTeams().size(), TEAMS / GROUPS);
        }
    }

    @Test(dependsOnMethods = {"addTeams"})
    public void createFights() {
        List<Fight> tournamentFights = treeTournamentHandler.createFights(tournamentConverter.reverse(tournamentDTO), TeamsOrder.NONE, 0, null);
        Assert.assertEquals(tournamentFights.size(), 4 * GROUPS);
        final List<Group> groups = groupController.getGroups(tournamentDTO, 0);
        for (final Group group : groups) {
            Assert.assertEquals(group.getFights().size(), 4);
        }
    }

    @Test(dependsOnMethods = {"createFights"})
    public void solveFights() {
        final List<Group> groups = groupController.getGroups(tournamentDTO, 0);

        // First group has team1 as winner.
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(0).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(0)).get(0).getName(), "Team01");


        // Second group has team6 winner.
        groups.get(1).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(1).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(1).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(1)).get(0).getName(), "Team06");

        // Third group has team11 winner.
        groups.get(2).getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(2).getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(2).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(2)).get(0).getName(), "Team11");

        // Forth group has team16 winner.
        groups.get(3).getFights().get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        groups.get(3).getFights().get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        groups.get(3).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(3)).get(0).getName(), "Team16");

    }

    @Test(dependsOnMethods = {"solveFights"})
    public void populateLevel1() {
        treeTournamentHandler.generateNextFights(tournamentConverter.reverse(tournamentDTO), null);

        //Check level1 groups' teams.
        final List<Group> groups = groupController.getGroups(tournamentDTO, 1);
        Assert.assertEquals(groups.size(), 2);

        //Group 1 -> Team01 vs Team06
        Assert.assertEquals(groups.get(0).getTeams().get(0).getName(), "Team01");
        Assert.assertEquals(groups.get(0).getTeams().get(1).getName(), "Team06");

        //Group 2 -> Team011 vs Team16
        Assert.assertEquals(groups.get(1).getTeams().get(0).getName(), "Team11");
        Assert.assertEquals(groups.get(1).getTeams().get(1).getName(), "Team16");
    }

    @AfterClass
    public void deleteTournament() {
        groupController.delete(tournamentDTO);
        fightController.delete(tournamentDTO);
        duelController.delete(tournamentDTO);
        teamController.delete(tournamentDTO);
        roleController.delete(tournamentDTO);
        tournamentController.delete(tournamentDTO);
        participantController.deleteAll();
        clubController.delete(clubDTO);
        Assert.assertEquals(fightController.count(), 0);
        Assert.assertEquals(duelController.count(), 0);
    }
}
