package edu.cmu.tepper.sme.gnp2.nav;

import java.util.Random;

import edu.cmu.tepper.sme.gnp2.GlacierNationalParkModelTest;
import edu.cmu.tepper.sme.gnp2.Heading;
import edu.cmu.tepper.sme.gnp2.ParkInfrastructure;
import edu.cmu.tepper.sme.gnp2.VisitList;
import edu.cmu.tepper.sme.gnp2.Visitor;
import edu.cmu.tepper.sme.gnp2.enumeration.Destinations;

public class StraightThroughNavigator extends Navigator
{
	private final Visitor v;
	private final Random r;
	private Heading heading;
	private int traversalTime;
	private boolean turnaroundProcessed = false;
	
	public StraightThroughNavigator( Visitor v, Heading h )
	{
		this.v = v;
		r = new Random();
		this.heading = h;
		traversalTime = -1;
	}
	
	@Override
	public Heading getHeading(){return heading;}
	
	@Override
	public void move( int time )
	{
		switch( v.state )
		{
			case DRIVING:
				// first see if the visitor has been on the road/attraction long enough to have traveled its entire length
				if( traversalTime > 0 )
				{
					traversalTime--;
					break;
				}
				
				switch( heading )
				{
					case EAST:
						v.setLocation( v.currentLocation().getEast() );
						break;
					case WEST:
						v.setLocation( v.currentLocation().getWest() );
						break;
					default:
						break;
				}
				
				traversalTime = v.currentLocation().traversalTime();
				v.cumulativeRoadTraversalTime += traversalTime;
				
				// always try to park if current location is in the list of target nodes
				if
				(
					v.currentLocation().isParkable() && // can only park at nodes with parking spots
					v.wantsToPark() &&                  // node to park at must be in destination list
					v.stops < v.profile.maxStops()      // can't stop of quota of stops has been reached
				)
				{
					if( v.currentLocation().park( v ) )
					{
						break;
					}
					else
					{
						v.logParking();
					}
				}
				
				break;
			case PARKED:
				v.parkTime--;
				if( v.parkTime <= 0 )
					v.currentLocation().leave( v );
				break;
			default:
				break;
		}
		
		if( v.targets.turnaround != null && !turnaroundProcessed )
		{
			if( v.currentLocation().getDestination() == v.targets.turnaround )
			{
				turnaroundProcessed = true;
				
				if( heading == Heading.EAST )
					heading = Heading.WEST;
				else
					heading = Heading.EAST;
			}
			
		}
		
		// Terminate this visitor when he gets to the sump pump node
		if( GlacierNationalParkModelTest.SUMP_PUMP_NAME.equals( v.currentLocation().toString() ) )
		{
			v.retire( time );
		}
	}

}
