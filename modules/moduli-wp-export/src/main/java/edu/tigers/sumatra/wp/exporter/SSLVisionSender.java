/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.exporter;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.MessagesRobocupSslDetection;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionBall;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionFrame;
import edu.tigers.sumatra.MessagesRobocupSslGeometry;
import edu.tigers.sumatra.MessagesRobocupSslGeometry.SSL_FieldLineSegment;
import edu.tigers.sumatra.MessagesRobocupSslGeometry.SSL_GeometryData;
import edu.tigers.sumatra.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import edu.tigers.sumatra.MessagesRobocupSslGeometry.Vector2f;
import edu.tigers.sumatra.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.MulticastUDPTransmitter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Send out SSL vision frames based on WorldFrames
 */
public class SSLVisionSender extends AModule implements IWorldFrameObserver
{
	private static final String ADDRESS = "224.5.23.2";
	private static final double GEOMETRY_BROADCAST_INTERVAL = 3;

	private MulticastUDPTransmitter transmitter;
	private int frameNumber = 0;
	private long tLastGeometrySent = 0;


	@Override
	public void startModule()
	{
		int port = getSubnodeConfiguration().getInt("port", 10006);
		transmitter = new MulticastUDPTransmitter(ADDRESS, port);

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{

		SSL_WrapperPacket.Builder wrapper = SSL_WrapperPacket.newBuilder();

		SSL_DetectionFrame.Builder frame = SSL_DetectionFrame.newBuilder();
		frame.setCameraId(0);
		frame.setFrameNumber(frameNumber++);
		double timestamp = wFrameWrapper.getTimestamp() / 1e9;
		frame.setTCapture(timestamp);
		frame.setTSent(timestamp);

		SSL_DetectionBall.Builder ball = SSL_DetectionBall.newBuilder();
		ball.setConfidence(1);
		ball.setX((float) wFrameWrapper.getSimpleWorldFrame().getBall().getPos3().x());
		ball.setY((float) wFrameWrapper.getSimpleWorldFrame().getBall().getPos3().y());
		ball.setZ((float) wFrameWrapper.getSimpleWorldFrame().getBall().getPos3().z());
		ball.setPixelX(0);
		ball.setPixelY(0);
		frame.addBalls(ball);

		for (ITrackedBot bot : wFrameWrapper.getSimpleWorldFrame().getBots().values())
		{
			MessagesRobocupSslDetection.SSL_DetectionRobot.Builder sslBot = MessagesRobocupSslDetection.SSL_DetectionRobot
					.newBuilder();
			sslBot.setConfidence(1);
			sslBot.setRobotId(bot.getBotId().getNumber());
			sslBot.setX((float) bot.getPos().x());
			sslBot.setY((float) bot.getPos().y());
			sslBot.setOrientation((float) bot.getOrientation());
			sslBot.setHeight(150);
			sslBot.setPixelX(0);
			sslBot.setPixelY(0);

			if (bot.getBotId().getTeamColor() == ETeamColor.YELLOW)
			{
				frame.addRobotsYellow(sslBot);
			} else
			{
				frame.addRobotsBlue(sslBot);
			}
		}
		wrapper.setDetection(frame);

		if ((System.nanoTime() - tLastGeometrySent) / 1e9 > GEOMETRY_BROADCAST_INTERVAL)
		{
			wrapper.setGeometry(createGeometryMessage());
			tLastGeometrySent = System.nanoTime();
		}

		transmitter.send(wrapper.build().toByteArray());
	}


	private SSL_GeometryData.Builder createGeometryMessage()
	{
		SSL_GeometryData.Builder geometry = SSL_GeometryData.newBuilder();
		SSL_GeometryFieldSize.Builder field = SSL_GeometryFieldSize.newBuilder();
		field.setBoundaryWidth((int) Geometry.getBoundaryWidth());
		field.setFieldLength((int) Geometry.getFieldLength());
		field.setFieldWidth((int) Geometry.getFieldWidth());
		field.setGoalDepth((int) Geometry.getGoalOur().getDepth());
		field.setGoalWidth((int) Geometry.getGoalOur().getWidth());

		SSL_FieldLineSegment.Builder penAreaLineStretchLeft = SSL_FieldLineSegment.newBuilder();
		Vector2f.Builder pl1 = Vector2f.newBuilder();
		pl1.setX((float) Geometry.getPenaltyMarkOur().x());
		pl1.setY((float) Geometry.getPenaltyAreaFrontLineLength() / 2);
		Vector2f.Builder pl2 = Vector2f.newBuilder();
		pl2.setX((float) Geometry.getPenaltyMarkOur().x());
		pl2.setY((float) -Geometry.getPenaltyAreaFrontLineLength() / 2);
		penAreaLineStretchLeft.setP1(pl1);
		penAreaLineStretchLeft.setP2(pl2);
		penAreaLineStretchLeft.setThickness(10);
		penAreaLineStretchLeft.setName("LeftPenaltyStretch");
		field.addFieldLines(penAreaLineStretchLeft);

		SSL_FieldLineSegment.Builder penAreaLineStretchRight = SSL_FieldLineSegment.newBuilder();
		Vector2f.Builder pr1 = Vector2f.newBuilder();
		pr1.setX((float) Geometry.getPenaltyMarkTheir().x());
		pr1.setY((float) Geometry.getPenaltyAreaFrontLineLength() / 2);
		Vector2f.Builder pr2 = Vector2f.newBuilder();
		pr2.setX((float) Geometry.getPenaltyMarkTheir().x());
		pr2.setY((float) -Geometry.getPenaltyAreaFrontLineLength() / 2);
		penAreaLineStretchRight.setP1(pr1);
		penAreaLineStretchRight.setP2(pr2);
		penAreaLineStretchRight.setThickness(10);
		penAreaLineStretchRight.setName("RightPenaltyStretch");
		field.addFieldLines(penAreaLineStretchRight);

		Geometry.getPenaltyAreaOur().getRectangle().getEdges().stream()
				.map(Lines::segmentFromLine)
				.map(l -> createLineSegment("", l))
				.forEach(field::addFieldLines);

		Geometry.getPenaltyAreaTheir().getRectangle().getEdges().stream()
				.map(Lines::segmentFromLine)
				.map(l -> createLineSegment("", l))
				.forEach(field::addFieldLines);

		Geometry.getField().getEdges().stream()
				.map(Lines::segmentFromLine)
				.map(l -> createLineSegment("", l))
				.forEach(field::addFieldLines);
		field.addFieldLines(createLineSegment("HalfwayLine",
				Lines.segmentFromPoints(
						Vector2.fromY(Geometry.getFieldWidth() / 2),
						Vector2.fromY(-Geometry.getFieldWidth() / 2))));
		field.addFieldLines(createLineSegment("CenterLine",
				Lines.segmentFromPoints(
						Vector2.fromX(Geometry.getFieldLength() / 2),
						Vector2.fromX(-Geometry.getFieldLength() / 2))));

		MessagesRobocupSslGeometry.SSL_FieldCicularArc.Builder centerCircle = MessagesRobocupSslGeometry.SSL_FieldCicularArc
				.newBuilder();
		centerCircle.setCenter(Vector2f.newBuilder().setX(0).setY(0).build());
		centerCircle.setA1(0);
		centerCircle.setA2((float) AngleMath.PI_TWO);
		centerCircle.setRadius((float) Geometry.getCenterCircle().radius());
		centerCircle.setThickness(10);
		centerCircle.setName("CenterCircle");
		field.addFieldArcs(centerCircle);

		geometry.setField(field);
		return geometry;
	}


	private SSL_FieldLineSegment.Builder createLineSegment(final String name, final ILineSegment line)
	{
		SSL_FieldLineSegment.Builder lineSegment = SSL_FieldLineSegment.newBuilder();
		Vector2f.Builder pr1 = Vector2f.newBuilder();
		pr1.setX((float) line.getStart().x());
		pr1.setY((float) line.getStart().y());
		Vector2f.Builder pr2 = Vector2f.newBuilder();
		pr2.setX((float) line.getEnd().x());
		pr2.setY((float) line.getEnd().y());
		lineSegment.setP1(pr1);
		lineSegment.setP2(pr2);
		lineSegment.setThickness(10);
		lineSegment.setName(name);
		return lineSegment;
	}
}
