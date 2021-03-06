/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.data;

import java.util.EnumMap;
import java.util.Map;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Construct new referee messages. All values are initialized already to make it easier to construct new messages
 * with only the relevant values.
 */
public class RefereeMsgBuilder
{
	private Map<ETeamColor, Referee.SSL_Referee.TeamInfo.Builder> teamBuilder = new EnumMap<>(ETeamColor.class);
	private Referee.SSL_Referee.Builder messageBuilder = Referee.SSL_Referee.newBuilder();


	private RefereeMsgBuilder()
	{
		initTeamInfo(ETeamColor.BLUE);
		initTeamInfo(ETeamColor.YELLOW);
		initMessage();
	}


	private void initTeamInfo(ETeamColor teamColor)
	{
		Referee.SSL_Referee.TeamInfo.Builder builder = Referee.SSL_Referee.TeamInfo.newBuilder();
		builder.setGoalkeeper(0);
		builder.setName(teamColor.name());
		builder.setScore(0);
		builder.setYellowCards(0);
		builder.setRedCards(0);
		builder.setTimeouts(4);
		builder.setTimeoutTime(360);
		teamBuilder.put(teamColor, builder);
	}


	private void initMessage()
	{
		messageBuilder.setPacketTimestamp(System.currentTimeMillis());
		messageBuilder.setCommand(Referee.SSL_Referee.Command.HALT);
		messageBuilder.setCommandCounter(-1);
		messageBuilder.setCommandTimestamp(0);
		messageBuilder.setStageTimeLeft(300);
		messageBuilder.setStage(Referee.SSL_Referee.Stage.NORMAL_FIRST_HALF);
	}


	/**
	 * @return a new builder
	 */
	public static RefereeMsgBuilder aRefereeMsg()
	{
		return new RefereeMsgBuilder();
	}


	/**
	 * @return a new referee message
	 */
	public Referee.SSL_Referee build()
	{
		messageBuilder.setBlue(teamBuilder.get(ETeamColor.BLUE));
		messageBuilder.setYellow(teamBuilder.get(ETeamColor.YELLOW));
		return messageBuilder.build();
	}


	/**
	 * @param pos in [mm] (not inverted!)
	 * @return this builder for chaining
	 */
	public RefereeMsgBuilder withBallPlacementPos(IVector2 pos)
	{
		if (pos != null)
		{
			Referee.SSL_Referee.Point.Builder point = Referee.SSL_Referee.Point.newBuilder();
			point.setX((float) pos.x());
			point.setY((float) pos.y());
			messageBuilder.setDesignatedPosition(point);
		} else
		{
			messageBuilder.clearDesignatedPosition();
		}
		return this;
	}


	/**
	 * @param command
	 * @return this builder for chaining
	 */
	public RefereeMsgBuilder withCommand(Referee.SSL_Referee.Command command)
	{
		messageBuilder.setCommand(command);
		return this;
	}


	/**
	 * @param teamColor
	 * @param score
	 * @return this builder for chaining
	 */
	public RefereeMsgBuilder withGoalScore(ETeamColor teamColor, int score)
	{
		teamBuilder.get(teamColor).setScore(score);
		return this;
	}


	/**
	 * @param timeLeft
	 * @return this builder for chaining
	 */
	public RefereeMsgBuilder withTimeLeft(final int timeLeft)
	{
		messageBuilder.setStageTimeLeft(timeLeft);
		return this;
	}


	/**
	 * @param commandCounter
	 * @return this builder for chaining
	 */
	public RefereeMsgBuilder withCommandCounter(final int commandCounter)
	{
		messageBuilder.setCommandCounter(commandCounter);
		return this;
	}


	/**
	 * @param timestamp
	 * @return this builder for chaining
	 */
	public RefereeMsgBuilder withPacketTimestamp(final long timestamp)
	{
		messageBuilder.setPacketTimestamp(timestamp);
		return this;
	}


	/**
	 * @param timestamp
	 * @return this builder for chaining
	 */
	public RefereeMsgBuilder withCommandTimestamp(final long timestamp)
	{
		messageBuilder.setCommandTimestamp(timestamp);
		return this;
	}


	/**
	 * @param stage
	 * @return this builder for chaining
	 */
	public RefereeMsgBuilder withStage(Referee.SSL_Referee.Stage stage)
	{
		messageBuilder.setStage(stage);
		return this;
	}

}
