/*
 *  Glacier National Park Visitor Parking Simulator
 *  Developed for Strategic Manaegement of the Enterprise (45-991) at Carnegie Mellon University, Fall 2017
 *  Copyright (C) 2017 Alexander P. Corn (alexcorn@gmail.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package edu.cmu.tepper.sme.gnp2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import edu.cmu.tepper.sme.gnp2.enumeration.Destinations;
import edu.cmu.tepper.sme.gnp2.enumeration.VisitorProfile;

public class VisitorSumpPump implements ParkInfrastructure, ActiveElement
{ 
	private List<Visitor> dead = new ArrayList<Visitor>();
	private ParkInfrastructure east;
	private ParkInfrastructure west;
	private final List<Visitor> activeVisitors;
	private List<Visitor> removeTheseVisitors;
	private final List<Visitor> redirectedVisitors;//man this is an ugly way to do this
	private final SortedSet<Visitor> waitingVisitors;// visitors that have been generated but aren't in the park yet
	private Random r = new Random();
	private GnpRandom gnpRand = new GnpRandom();
	
	private int visitorsGenerated = 0;
	
	public int getNormDist( int mean, int std )
	{
		return gnpRand.norm( mean, std );
	}
	
	public VisitorSumpPump()
	{
		this.activeVisitors = new LinkedList<Visitor>();
		this.redirectedVisitors = new LinkedList<Visitor>();
		this.waitingVisitors = new TreeSet<Visitor>();
		this.removeTheseVisitors = new LinkedList<Visitor>();
	}
	
	public void init()
	{
		for( VisitorProfile vp : VisitorProfile.values() )
		{
			int tempGenerated = 0;
			while( tempGenerated < vp.numGenerate() )
			{
				Pair<Heading,ParkInfrastructure> startLoc = getStartLocation( vp );
				Visitor v = new Visitor( this, gnpRand, vp, this, visitorsGenerated, startLoc.getLeft() );
//				v.setLocation( startLoc.getRight() );
				visitorsGenerated++;
				tempGenerated++;
				waitingVisitors.add( v );
			}
		}
		
		System.out.println( "GENERATED " + visitorsGenerated + " VISITORS" );
	}
	
	public void retireVisitor( Visitor v )
	{
		dead.add( v );
		removeTheseVisitors.add( v );
	}
	
	private ParkInfrastructure whichStart( Visitor v )
	{
		if( v.getHeading() == Heading.EAST )
			return this.east;
		else
			return this.west;
	}

	public void update( int time )
	{
		
		Iterator<Visitor> iter = waitingVisitors.iterator();
		
		while( iter.hasNext() )
		{
			Visitor v = iter.next();
			if( v.entryTime <= time )
			{
				// Determine whether to let the visitor into the regular park
				// or make him go into an aux lot
				if( activeVisitors.size() < 2000 || !whichStart( v ).hasParking() )// TODO this threshold
				{
					activeVisitors.add( v );
				}
				else
				{
					v.overrideTargets( whichStart( v ).getDestination() );
					redirectedVisitors.add( v );
				}
				
				iter.remove();
			}
			else
			{
				break;
			}
		}
		
//		System.out.format( "%04d, %04d, %04d, %04d, %04d%n", time, waitingVisitors.size(), activeVisitors.size(), redirectedVisitors.size(), dead.size() );
		
		for( Visitor v : activeVisitors )
		{
			v.update( time );
		}
		
		for( Visitor v : redirectedVisitors )
		{
			v.update( time );
		}
		
		activeVisitors.removeAll( removeTheseVisitors );
		redirectedVisitors.removeAll( removeTheseVisitors );
		removeTheseVisitors = new LinkedList<Visitor>();
	}
	
	public Pair<Heading,ParkInfrastructure> getStartLocation( VisitorProfile vp )
	{
		if( r.nextFloat() < vp.westPortion() )
			return new ImmutablePair<Heading,ParkInfrastructure>( Heading.EAST, this );
		return new ImmutablePair<Heading,ParkInfrastructure>( Heading.WEST, this );
	}
	
	public Destinations getDestination()
	{
		return null;
	}

	public void setEast( ParkInfrastructure r )
	{
		this.east = r;
	}
	public void setWest( ParkInfrastructure r )
	{
		this.west = r;
	}
	public ParkInfrastructure getEast()
	{
		return east;
	}
	public ParkInfrastructure getWest()
	{
		return west;
	}
	
	public String toString()
	{
		return GlacierNationalParkModelTest.SUMP_PUMP_NAME;
	}
	
	public void dump( int time, BufferedWriter bw )
	{
		try
		{
//			bw.write( "Visitors generated: " + visitorsGenerated + "\n" );
	//		System.out.println( "visitor_id, entry_time, exit_time, time_in_park, cumulative_drive_time, cumulative_park_time, path, logan_park_attempts, logan_park_successes, logan_park_total_time, loop_park_attempts, loop_park_successes, loop_park_total_time" );
			bw.write( "visitor_id, entry_time, exit_time, time_in_park, cumulative_drive_time, cumulative_park_time, path, profile" );
			// logan_park_attempts, logan_park_successes, logan_park_total_time, loop_park_attempts, loop_park_successes, loop_park_total_time
			SortedSet<ParkInfrastructure> nodes = GlacierNationalParkModelTest.buildParkableNodeList( this );
			
			for( ParkInfrastructure pi : nodes )
			{
				String name = pi.toString().replaceAll( " ", "_" ).toLowerCase();
				bw.write( ", " + name + "_park_attempts," + name + "_park_successes, " + name + "_park_total_time" );
			}
			
			bw.write( ",routeId\n" );
			
			for( Visitor v : activeVisitors )
				v.dump( bw );
			for( Visitor v : redirectedVisitors )
				v.dump( bw );
			for( Visitor v : dead )
				v.dump( bw );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
	}
	
	public boolean park( Visitor v )
	{
		return false;
	}
	public void leave( Visitor v )
	{
		
	}
	public boolean isParkable(){return false;}
	public int traversalTime(){return 1;}

	public ImmutablePair<Double, Double> getLatLong()
	{
		return new ImmutablePair<Double, Double>( -100.0, -100.0 );
	}
	
	public void traverse( Visitor v ){}
	public void endTraverse( Visitor v ){}
	public boolean hasParking(){return false;}
}
