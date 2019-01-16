/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


@Persistent
public class AttackerDoubleTouchedBall extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected AttackerDoubleTouchedBall()
	{
		team = null;
		bot = 0;
		location = null;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public AttackerDoubleTouchedBall(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getAttackerDoubleTouchedBall().getByTeam());
		this.bot = event.getAttackerDoubleTouchedBall().getByBot();
		this.location = toVector(event.getAttackerDoubleTouchedBall().getLocation());
	}
	
	
	public AttackerDoubleTouchedBall(BotID bot, IVector2 location)
	{
		super(EGameEvent.ATTACKER_DOUBLE_TOUCHED_BALL);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.ATTACKER_DOUBLE_TOUCHED_BALL);
		builder.getAttackerDoubleTouchedBallBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Attacked %d %s double touched ball @ %s", bot, team, formatVector(location));
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final AttackerDoubleTouchedBall that = (AttackerDoubleTouchedBall) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(team, that.team)
				.append(location, that.location)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(bot)
				.append(location)
				.toHashCode();
	}
}
