/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.ETeamColor;


@Persistent
public class BotSubstitution extends AGameEvent
{
	private final ETeamColor team;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected BotSubstitution()
	{
		team = null;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotSubstitution(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotSubstitution().getByTeam());
	}
	
	
	public BotSubstitution(ETeamColor team)
	{
		super(EGameEvent.BOT_SUBSTITUTION);
		this.team = team;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.BOT_SUBSTITUTION);
		builder.getBotSubstitutionBuilder().setByTeam(getTeam(team));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Team %s wants to substitute a bot", team);
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final BotSubstitution that = (BotSubstitution) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(team, that.team)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.toHashCode();
	}
}
