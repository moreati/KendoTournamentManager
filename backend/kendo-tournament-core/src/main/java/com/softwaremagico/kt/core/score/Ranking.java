package com.softwaremagico.kt.core.score;

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


import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.persistence.entities.ScoreType;

import java.util.*;

public class Ranking {

    private final GroupDTO group;
    private List<TeamDTO> teamRanking = null;
    private List<ParticipantDTO> participants = null;
    private List<ScoreOfTeam> teamScoreRanking = null;
    private List<ScoreOfCompetitor> competitorsScoreRanking = null;

    public Ranking(GroupDTO group) {
        this.group = group;
    }

    public List<TeamDTO> getTeamsRanking() {
        if (teamRanking == null) {
            teamRanking = getTeamsRanking(group);
        }
        return teamRanking;
    }

    public List<ScoreOfTeam> getTeamsScoreRanking() {
        if (teamScoreRanking == null) {
            teamScoreRanking = getTeamsScoreRanking(group);
        }
        return teamScoreRanking;
    }

    /**
     * Return a Hashmap that classify the teams by position (1st, 2nd, 3rd,...)
     *
     * @return classification of the teams
     */
    private HashMap<Integer, List<TeamDTO>> getTeamsByPosition() {
        final HashMap<Integer, List<TeamDTO>> teamsByPosition = new HashMap<>();
        final List<ScoreOfTeam> scores = getTeamsScoreRanking();

        Integer position = 0;
        for (int i = 0; i < scores.size(); i++) {
            teamsByPosition.computeIfAbsent(position, k -> new ArrayList<>());
            // Put team in position.
            teamsByPosition.get(position).add(scores.get(i).getTeam());
            // Different score with next team.
            if ((i < scores.size() - 1) && scores.get(i).compareTo(scores.get(i + 1)) != 0) {
                position++;
            }
        }

        return teamsByPosition;
    }

    public List<TeamDTO> getFirstTeamsWithDrawScore(Integer maxWinners) {
        final HashMap<Integer, List<TeamDTO>> teamsByPosition = getTeamsByPosition();
        for (int i = 0; i < maxWinners; i++) {
            final List<TeamDTO> teamsInDraw = teamsByPosition.get(i);
            if (teamsInDraw.size() > 1) {
                return teamsInDraw;
            }
        }
        return null;
    }

    public TeamDTO getTeam(Integer order) {
        final List<TeamDTO> teamsOrder = getTeamsRanking();
        if (order >= 0 && order < teamsOrder.size()) {
            return teamsOrder.get(order);
        }
        return null;
    }

    public ScoreOfTeam getScoreOfTeam(Integer order) {
        final List<ScoreOfTeam> teamsOrder = getTeamsScoreRanking();
        if (order >= 0 && order < teamsOrder.size()) {
            return teamsOrder.get(order);
        }
        return null;
    }

    public List<ParticipantDTO> getParticipants() {
        if (participants == null) {
            participants = getCompetitorsRanking(group);
        }
        return participants;
    }

    public List<ScoreOfCompetitor> getCompetitorsScoreRanking() {
        if (competitorsScoreRanking == null) {
            competitorsScoreRanking = getCompetitorsScoreRanking(group);
        }
        return competitorsScoreRanking;
    }

    public ScoreOfCompetitor getScoreRanking(ParticipantDTO competitor) {
        final List<ScoreOfCompetitor> scoreRanking = getCompetitorsScoreRanking();
        for (final ScoreOfCompetitor score : scoreRanking) {
            if (score.getCompetitor().equals(competitor)) {
                return score;
            }
        }
        return null;
    }

    public ParticipantDTO getCompetitor(Integer order) {
        final List<ParticipantDTO> competitorOrder = getParticipants();
        if (order >= 0 && order < competitorOrder.size()) {
            return competitorOrder.get(order);
        }
        return null;
    }

    public ScoreOfCompetitor getScoreOfCompetitor(Integer order) {
        final List<ScoreOfCompetitor> teamsOrder = getCompetitorsScoreRanking();
        if (order >= 0 && order < teamsOrder.size()) {
            return teamsOrder.get(order);
        }
        return null;
    }

    private static Set<ParticipantDTO> getParticipants(List<TeamDTO> teams) {
        final Set<ParticipantDTO> allCompetitors = new HashSet<>();
        for (final TeamDTO team : teams) {
            allCompetitors.addAll(team.getMembers());
        }
        return allCompetitors;
    }

    public static List<TeamDTO> getTeamsRanking(GroupDTO group) {
        final List<ScoreOfTeam> scores = getTeamsScoreRanking(group);
        final List<TeamDTO> teamRanking = new ArrayList<>();
        for (final ScoreOfTeam score : scores) {
            teamRanking.add(score.getTeam());
        }
        return teamRanking;
    }

    public static List<ScoreOfTeam> getTeamsScoreRanking(GroupDTO group) {
        final List<TeamDTO> teamsOfFights = group.getTeams();
        final List<ScoreOfTeam> scores = new ArrayList<>();
        for (final TeamDTO team : teamsOfFights) {
            scores.add(ScoreOfTeam.getScoreOfTeam(team, group.getFights(), group.getUnties()));
        }
        Collections.sort(scores);

        return scores;
    }

    /**
     * Gets the more restrictive score for obtaining the ranking.
     *
     * @param competitor
     * @param group
     * @return
     */
    private static ScoreOfCompetitor getScoreOfCompetitor(ParticipantDTO competitor, GroupDTO group) {
        // If one fight is classic, use classic score.
        if (group.getTournament().getTournamentScore().getScoreType().equals(ScoreType.CLASSIC)) {
            return new ScoreOfCompetitorClassic(competitor, group.getFights());
        }

        // If one fight is european, use european score
        if (group.getTournament().getTournamentScore().getScoreType().equals(ScoreType.EUROPEAN)) {
            return new ScoreOfCompetitorEuropean(competitor, group.getFights());
        }

        // If one fight is european, use european score
        if (group.getTournament().getTournamentScore().getScoreType().equals(ScoreType.INTERNATIONAL)) {
            return new ScoreOfCompetitorInternational(competitor, group.getFights());
        }

        // If one fight is winOverDraw, use winOverDraw score
        if (group.getTournament().getTournamentScore().getScoreType().equals(ScoreType.WIN_OVER_DRAWS)) {
            return new ScoreOfCompetitorWinOverDraws(competitor, group.getFights());
        }

        return new ScoreOfCompetitorCustom(competitor, group.getFights());
    }

    public static ScoreOfCompetitor getScoreRanking(ParticipantDTO competitor, GroupDTO group) {
        return getScoreOfCompetitor(competitor, group);
    }

    public static List<ScoreOfCompetitor> getCompetitorsScoreRanking(GroupDTO group) {
        final Set<ParticipantDTO> competitors = getParticipants(group.getTeams());
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        for (final ParticipantDTO competitor : competitors) {
            scores.add(getScoreOfCompetitor(competitor, group));
        }
        Collections.sort(scores);
        return scores;
    }

    public static Integer getOrder(GroupDTO group, TeamDTO team) {
        final List<TeamDTO> ranking = getTeamsRanking(group);

        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).equals(team)) {
                return i;
            }
        }
        return null;
    }

    public static Integer getOrderFromRanking(List<ScoreOfTeam> ranking, TeamDTO team) {
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getTeam().equals(team)) {
                return i;
            }
        }
        return null;
    }

    public static TeamDTO getTeam(GroupDTO group, Integer order) {
        final List<TeamDTO> ranking = getTeamsRanking(group);
        if (order < ranking.size() && order >= 0) {
            return ranking.get(order);
        }
        return null;
    }

    public static List<ParticipantDTO> getCompetitorsRanking(GroupDTO group) {
        final Set<ParticipantDTO> competitors = getParticipants(group.getTeams());
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        for (final ParticipantDTO competitor : competitors) {
            scores.add(getScoreOfCompetitor(competitor, group));
        }
        Collections.sort(scores);
        final List<ParticipantDTO> competitorsRanking = new ArrayList<>();
        for (final ScoreOfCompetitor score : scores) {
            competitorsRanking.add(score.getCompetitor());
        }
        return competitorsRanking;
    }

    @Override
    public String toString() {
        return getTeamsRanking().toString();
    }
}
