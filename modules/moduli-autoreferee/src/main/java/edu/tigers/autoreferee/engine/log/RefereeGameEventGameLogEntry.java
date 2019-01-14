package edu.tigers.autoreferee.engine.log;

import com.google.protobuf.TextFormat;

import edu.tigers.sumatra.SslGameEvent;


public class RefereeGameEventGameLogEntry extends GameLogEntry
{
	private final SslGameEvent.GameEvent gameEvent;
	
	
	public RefereeGameEventGameLogEntry(
			final long timestamp,
			final GameTime gameTime,
			final SslGameEvent.GameEvent gameEvent)
	{
		super(ELogEntryType.RECEIVED_GAME_EVENT, timestamp, gameTime);
		this.gameEvent = gameEvent;
	}
	
	
	@Override
	public String workGameLogEntry()
	{
		return TextFormat.shortDebugString(gameEvent);
	}
	
	
	@Override
	public String getToolTipText()
	{
		return "Received a new referee message with command " + gameEvent.getType();
	}
}