/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBall;


/**
 * Abstract base class that contains common operations for the game rules
 */
public abstract class AGameEventDetector implements IGameEventDetector
{
	private final Set<EGameState> activeStates;
	private final EGameEventDetectorType type;
	private boolean firstUpdate = true;
	protected IAutoRefFrame frame;
	
	
	/**
	 * @param gameState The gameState this rule will be active in
	 */
	protected AGameEventDetector(final EGameEventDetectorType type, final EGameState gameState)
	{
		this(type, EnumSet.of(gameState));
	}
	
	
	/**
	 * @param activeStates the list of game states that the rule will be active in
	 */
	protected AGameEventDetector(final EGameEventDetectorType type, final Set<EGameState> activeStates)
	{
		this.type = type;
		this.activeStates = activeStates;
	}
	
	
	@Override
	public final Optional<IGameEvent> update(final IAutoRefFrame frame)
	{
		this.frame = frame;
		if (firstUpdate)
		{
			doPrepare();
			firstUpdate = false;
		}
		return doUpdate();
	}
	
	
	@Override
	public final void reset()
	{
		firstUpdate = true;
		doReset();
	}
	
	
	protected void doPrepare()
	{
	}
	
	
	protected abstract Optional<IGameEvent> doUpdate();
	
	
	protected void doReset()
	{
	}
	
	
	@Override
	public boolean isActiveIn(final EGameState state)
	{
		return activeStates.contains(state);
	}
	
	
	@Override
	public EGameEventDetectorType getType()
	{
		return type;
	}
	
	
	protected final ITrackedBall getBall()
	{
		return frame.getWorldFrame().getBall();
	}
	
	
	protected static void registerClass(final Class<?> clazz)
	{
		ConfigRegistration.registerClass("autoreferee", clazz);
	}
}
