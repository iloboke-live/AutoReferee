/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.calc;

import java.awt.Color;
import java.util.List;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Circle;


public class AllowedDistancesVisCalc implements IRefereeCalc
{
	@Override
	public void process(final AutoRefFrame frame)
	{
		List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.ALLOWED_DISTANCES);
		
		if (frame.getGameState().isStandardSituation())
		{
			NGeometry.getPenaltyAreas().stream()
					.map(p -> p.withMargin(RuleConstraints.getBotToPenaltyAreaMarginStandard()).getRectangle())
					.map(r -> new DrawableRectangle(r, Color.red))
					.forEach(shapes::add);
		}
		
		if (frame.getGameState().isStoppedGame())
		{
			shapes.add(new DrawableCircle(Circle.createCircle(frame.getWorldFrame().getBall().getPos(),
					RuleConstraints.getStopRadius() + Geometry.getBallRadius())));
		}
	}
}
