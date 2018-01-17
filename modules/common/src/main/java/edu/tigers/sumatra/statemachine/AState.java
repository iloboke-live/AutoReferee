/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statemachine;

/**
 * A state with a default identifier
 */
public class AState implements IState
{
	private final String defaultIdentifier = this.getClass().getSimpleName();
	
	
	@Override
	public String getIdentifier()
	{
		return defaultIdentifier;
	}
}
