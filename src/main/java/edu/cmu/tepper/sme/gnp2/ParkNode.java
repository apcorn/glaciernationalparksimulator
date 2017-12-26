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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;

import edu.cmu.tepper.sme.gnp2.enumeration.Destinations;
import edu.cmu.tepper.sme.gnp2.enumeration.VisitorState;

public class ParkNode implements ParkInfrastructure, Comparable<ParkNode>
{
	private ParkInfrastructure east;
	private ParkInfrastructure west;
	private String name;
	private final int traverseMean;
	private final int traverseStd;
	public final int mileMarker;
	private final GnpRandom r;
	private final Destinations destination;
	private final ImmutablePair<Double, Double> latLong;
	
	private int parkAttempts = 0;
	private int parkSuccesses = 0;
	
	private Map<Integer,List<Visitor>> parking = new HashMap<Integer,List<Visitor>>();
	private List<Visitor> traversing = new LinkedList<Visitor>();
	public final int parkingLimit;
	
	public ParkNode( int mileMarker, int traverseMean, int traverseStd, Destinations destination, double lat, double lon )
	{
		this( destination.toString(), mileMarker, traverseMean, traverseStd, destination, lat, lon );
	}
	
	public ParkNode( String name, int mileMarker, int traverseMean, int traverseStd, Destinations destination, double lat, double lon )
	{
		this.destination = destination;
		this.traverseMean = traverseMean;
		this.traverseStd = traverseStd;
//		this.parkMean = parkMean;
//		this.parkStd = parkStd;
		this.mileMarker = mileMarker;
		this.name = name;
		if( destination != null )
		{
			// init parking
			for( ParkingTier pt : destination.parkingTiersNoBs() )
				parking.put( new Integer( pt.tier ), new ArrayList<Visitor>() );
			this.parkingLimit = destination.totalParkingSpaces();
		}
		else
		{
			this.parkingLimit = 0;
		}
		
		r = new GnpRandom();
		this.latLong = new ImmutablePair<Double, Double>( lat, lon );
		
	}
	public boolean hasParking()
	{
		for( ParkingTier pt : destination.parkingTiersNoBs() )
		{
			if( parking.get( pt.tier ).size() < pt.capacity )
			{
				return true;
			}
		}
		
		return false;
	}
	public boolean isParkable()
	{
		return parkingLimit > 0;
	}
	public void traverse( Visitor v )
	{
		traversing.add( v );
	}
	public void endTraverse( Visitor v )
	{
		traversing.remove( v );
	}
	public boolean park( Visitor v )
	{
		int timeToPark = r.norm( v.profile.getParkTimeMean( destination ), v.profile.getParkTimeStdv( destination ) );
		if( this.toString() == "WEST_LOT" )
		{
			System.out.println( "Time to park: " + timeToPark  );
		}
		parkAttempts++;
		
		for( ParkingTier pt : destination.parkingTiers( v.profile ) )
		{
			if( parking.get( pt.tier ).size() < pt.capacity && ( timeToPark <= pt.timeLimit || this.destination.isAdaptive( v.profile ) ) )
			{
				if( timeToPark > pt.timeLimit )
					timeToPark = pt.timeLimit;
				parkSuccesses++;
				parking.get( pt.tier ).add( v );
				v.state = VisitorState.PARKED;
				v.parkTime = timeToPark;
				v.stops++;
				v.parkingTier = pt.tier;
				v.logParking();
				return true;
			}
		}
		
		return false;
	}
	public void leave( Visitor v )
	{
		parking.get( v.parkingTier ).remove( v );
		v.state = VisitorState.DRIVING;
		v.parkTime = 0;
		v.parkingTier = -1;
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
		return name;
	}
	public int compareTo( ParkNode o )
	{
		return toString().compareTo( o.toString() );
	}
	public int traversalTime()
	{
		return r.norm( traverseMean, traverseStd );
	}
	public void dump( int time, BufferedWriter bw )
	{
		
		try
		{
			bw.write( String.format( "%s, %d, %d, %d, %d, %.02f, %.04f, %.04f, %d, %d%n", name, time, parkAttempts, parkSuccesses, numParked(), (float)numParked() / (float)parkingLimit, latLong.left, latLong.right, traversing.size(), parking.size() ) );
			parkAttempts = 0;
			parkSuccesses = 0;
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit( -1 );
		}
	}
	public int numParked()
	{
		int numParked = 0;
		
		Iterator<Entry<Integer,List<Visitor>>> iter = parking.entrySet().iterator();
		while( iter.hasNext() )
		{
			Entry<Integer,List<Visitor>> i = iter.next();
			numParked += parking.get( i.getKey() ).size();
		}
		
		return numParked;
	}
	public ImmutablePair<Double, Double> getLatLong()
	{
		return latLong;
	}
	public Destinations getDestination()
	{
		return destination;
	}
}
