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
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import edu.cmu.tepper.sme.gnp2.enumeration.Destinations;
import edu.cmu.tepper.sme.gnp2.enumeration.VisitorProfile;
import edu.cmu.tepper.sme.gnp2.enumeration.VisitorState;
import edu.cmu.tepper.sme.gnp2.nav.Navigator;
import edu.cmu.tepper.sme.gnp2.nav.StraightThroughNavigator;

public class Visitor implements ActiveElement, Comparable<Visitor>
{
	// model attributes
	//
	// entry time
	// list of destinations and associated preference for stopping
	// average supplement (add to attraction average time spent)
	// std dev supplement (add to attraction std dev time spent)
	// time budget (max time spent in park, either in hours or a time of day)
	// parking wait sensitivity (in minutes) (might be modified by preference for stopping at each location)
	// entry/exit nodes
	// traversal strategy
	//   maintain heading until current node matches unvisited node in visit list
	//   traverse park until current node is highest-preference unvisited node in visit list
	
	public final int entryTime;
	private int exitTime = Integer.MAX_VALUE;
	private final VisitorSumpPump vsp;
	
	public final VisitorProfile profile;
	private Deque<ParkInfrastructure> route;
	private ParkInfrastructure currentLocation;
	private int id;
	private boolean retired = false;
	
	private Navigator navigator;
	public VisitorState state = VisitorState.DRIVING;
	
	private List<Pair<ParkInfrastructure,Integer>> parkLog = new LinkedList<Pair<ParkInfrastructure,Integer>>();
	public int parkTime;
	public int parkingTier; // need to know which parking tier parked at - used to retrieve parking list from parking tier hash table. so ugly. i dont like doing this :(
	public int stops = 0;
	public int cumulativeRoadTraversalTime = 0;
	private int cumulativeParkingTime = 0;
	public VisitList targets;
	
	public Visitor( VisitorSumpPump vsp, GnpRandom r, VisitorProfile profile, ParkInfrastructure start, int id, Heading h )
	{
		this.vsp = vsp;
		this.profile = profile;
		this.entryTime = r.norm( profile.getEntryTimeMean(), profile.getEntryTimeStd() );
		this.id = id;
		this.route = new LinkedList<ParkInfrastructure>();
		this.currentLocation = start;
		this.targets = profile.randomVisitList();
		this.navigator = new StraightThroughNavigator( this, h );
	}
	
	public Heading getHeading()
	{
		return navigator.getHeading();
	}
	
	public void overrideTargets( Destinations d )
	{
		List<Destinations> newList = new LinkedList<Destinations>();
		newList.add( d );
		targets = new VisitList( newList, d, 99 );
	}
	
	public void setLocation( ParkInfrastructure pi )
	{
		currentLocation().endTraverse( this );
		
		route.add( currentLocation );
		currentLocation = pi;
		
		currentLocation().traverse( this );
	}
	
	public ParkInfrastructure currentLocation()
	{
		return currentLocation;
	}
	
	public void logParking()
	{
		cumulativeParkingTime += parkTime;
		parkLog.add( new ImmutablePair<ParkInfrastructure,Integer>( currentLocation, parkTime ) );
	}
	
	public String toString()
	{
		return String.format( "visitor %d (entry time %d, exit time %d, time in park %d)", id, entryTime, exitTime, exitTime - entryTime );
	}

	public void update( int time )
	{
		if( retired ) return;
		
		navigator.move( time );
	}

	public void dump( BufferedWriter bw ) throws IOException
	{
		Map<ParkInfrastructure, MutableTriple<Integer, Integer, Integer>> parkingData = new HashMap<ParkInfrastructure, MutableTriple<Integer,Integer,Integer>>();
		
		for( ParkInfrastructure pi : GlacierNationalParkModelTest.buildParkableNodeList( currentLocation ) )
		{
			parkingData.put( pi, new MutableTriple<Integer, Integer, Integer>( 0, 0, 0 ) );
		}
		
		for( Pair<ParkInfrastructure,Integer> pl : parkLog )
		{
			if( !parkingData.containsKey( pl.getLeft() ) )
			{
				System.err.println( "PARKED SOMEPLACE NOT PARKABLE???" );
			}

			MutableTriple<Integer, Integer, Integer> mt = parkingData.get( pl.getLeft() );

			int success = pl.getRight() > 0 ? 1 : 0;
			
			mt.left++; //attempts
			mt.middle += success; //successes
			mt.right += pl.getRight(); //total time spent parked here
			
			parkingData.put( pl.getLeft(), mt );
		}

		List<Object> args = new LinkedList<Object>();
		args.add( id );
		args.add( entryTime );
		args.add( exitTime );
		args.add( exitTime - entryTime );
		args.add( cumulativeRoadTraversalTime );
		args.add( cumulativeParkingTime );
		args.add( buildPath() );
		args.add( profile.toString() );
		
		int nodeCount = 0;
		
		for( ParkInfrastructure pi : GlacierNationalParkModelTest.buildParkableNodeList( currentLocation ) )
		{
			Triple<Integer,Integer,Integer> t = parkingData.get( pi );
			args.add( t.getLeft() );
			args.add( t.getMiddle() );
			args.add( t.getRight() );
			nodeCount++;// for determining number of %d sequences to add to end of format string
		}
		
		StringBuilder baseFormat = new StringBuilder();
		baseFormat.append( "%d, %d, %d, %d, %d, %d, %s, %s" );

		// what I'm doing here is building a big long format string dynamically, based on the number of nodes that need to be output in the .csv file
		for( int i = 0; i < nodeCount; i++ )
		{
			baseFormat.append( ", %d, %d, %d" );
		}
		
		// have to tack on the routeId at the end, i hope this doesn't screw anything up.
		// UPDATE: it didnt, apparently
		args.add( targets.routeId );
		baseFormat.append( ",%d%n" );
		
		bw.write( String.format( baseFormat.toString(), args.toArray() ) );
		
	}
	

	
	public String buildPath()
	{
		StringBuilder sb = new StringBuilder();
		
		for( ParkInfrastructure pi : route )
		{
			sb.append( "\"" );
			sb.append( pi );
			sb.append( "\"" );
			sb.append( " " );
		}
		
		return sb.toString().trim();
	}
	
	public void retire( int time )
	{
		this.exitTime = time;
		this.retired = true;
		vsp.retireVisitor( this );
	}

	public int compareTo( Visitor o )
	{
		if( entryTime != o.entryTime )
			return new Integer( entryTime ).compareTo( new Integer( o.entryTime ) );
		
		return new Integer( id ).compareTo( new Integer( o.id ) );
	}

	public boolean wantsToPark()
	{
		return targets.list.contains( currentLocation.getDestination() );
	}
}
