/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * Data structure for a filtered robot.
 */
@Persistent
public class FilteredVisionBot
{
	private final BotID botID;
	/** [mm,mm] */
	private final IVector2 pos;
	/** [m/s,m/s] */
	private final IVector2 vel;
	/** [rad] */
	private final double orientation;
	/** [rad/s] */
	private final double angularVel;
	/** 0-1 */
	private final double quality;


	@SuppressWarnings("unused") // for Berkeley database
	private FilteredVisionBot()
	{
		botID = BotID.noBot();
		pos = Vector2f.ZERO_VECTOR;
		vel = Vector2f.ZERO_VECTOR;
		orientation = 0;
		angularVel = 0;
		quality = 0;
	}


	private FilteredVisionBot(final BotID botID, final IVector2 pos, final IVector2 vel,
			final double orientation, final double angularVel, final double quality)
	{
		this.botID = botID;
		this.pos = pos;
		this.vel = vel;
		this.orientation = orientation;
		this.angularVel = angularVel;
		this.quality = quality;
	}


	public BotID getBotID()
	{
		return botID;
	}


	/**
	 * @return [mm, mm]
	 */
	public IVector2 getPos()
	{
		return pos;
	}


	/**
	 * @return [m/s, m/s]
	 */
	public IVector2 getVel()
	{
		return vel;
	}


	/**
	 * @return [rad]
	 */
	public double getOrientation()
	{
		return orientation;
	}


	/**
	 * @return [rad/s]
	 */
	public double getAngularVel()
	{
		return angularVel;
	}


	public double getQuality()
	{
		return quality;
	}


	/**
	 * Extrapolate bot into future.
	 *
	 * @param timestampNow
	 * @param timestampFuture
	 * @return
	 */
	public FilteredVisionBot extrapolate(final long timestampNow, final long timestampFuture)
	{
		if (timestampFuture < timestampNow)
		{
			return this;
		}

		double dt = (timestampFuture - timestampNow) * 1e-9;

		return Builder.create()
				.withId(botID)
				.withQuality(quality)
				.withPos(pos.addNew(vel.multiplyNew(dt * 1e3)))
				.withVel(vel)
				.withOrientation(AngleMath.normalizeAngle(orientation + (angularVel * dt)))
				.withAVel(angularVel)
				.build();
	}


	public BotState toBotState()
	{
		return BotState.of(botID, State.of(Pose.from(pos, orientation), Vector3.from2d(vel, angularVel)));
	}


	@Override
	public String toString()
	{
		return "FilteredVisionBot{" +
				"botID=" + botID +
				", pos=" + pos +
				", vel=" + vel +
				", orientation=" + orientation +
				", angularVel=" + angularVel +
				'}';
	}

	/**
	 * Builder for sub class
	 */
	public static final class Builder
	{
		private BotID botID;
		/** [mm,mm] */
		private IVector2 pos;
		/** [m/s,m/s] */
		private IVector2 vel;
		/** [rad] */
		private Double orientation;
		/** [rad/s] */
		private Double angularVel;
		/** 0-1 */
		private double quality = 0;


		private Builder()
		{
		}


		/**
		 * @return new builder
		 */
		public static Builder create()
		{
			return new Builder();
		}


		/**
		 * @param botID id of the bot
		 * @return this builder
		 */
		public Builder withId(final BotID botID)
		{
			this.botID = botID;
			return this;
		}


		/**
		 * @param pos of bot
		 * @return this builder
		 */
		public Builder withPos(final IVector2 pos)
		{
			this.pos = pos;
			return this;
		}


		/**
		 * @param vel of bot [m/s,m/s]
		 * @return this builder
		 */
		public Builder withVel(final IVector2 vel)
		{
			this.vel = vel;
			return this;
		}


		/**
		 * @param orientation of bot
		 * @return this builder
		 */
		public Builder withOrientation(final double orientation)
		{
			this.orientation = orientation;
			return this;
		}


		/**
		 * @param aVel of bot
		 * @return this builder
		 */
		public Builder withAVel(final double aVel)
		{
			angularVel = aVel;
			return this;
		}


		/**
		 * @param quality of bot
		 * @return this builder
		 */
		public Builder withQuality(final double quality)
		{
			this.quality = quality;
			return this;
		}


		/**
		 * @return new instance
		 */
		public FilteredVisionBot build()
		{
			Validate.notNull(botID);
			Validate.notNull(pos);
			Validate.notNull(vel);
			Validate.notNull(orientation);
			Validate.notNull(angularVel);
			return new FilteredVisionBot(botID, pos, vel, orientation, angularVel, quality);
		}
	}
}
