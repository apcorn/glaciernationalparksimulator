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
package edu.cmu.tepper.sme.gnp2.enumeration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutablePair;

import edu.cmu.tepper.sme.gnp2.VisitList;

// if i could do this over the visitor profiles would be defined in a config file and loaded at runtime, not implemented as enums
public enum VisitorProfile
{
	LOCAL_DAY_HIKE_MODE1_W,
	LOCAL_DAY_HIKE_MODE2_W,
	TOUR_DAY_HIKE_MODE1_W,
	TOUR_DAY_HIKE_MODE2_W,
	INDEP_RTV_MODE1_W,
	INDEP_RTV_MODE2_W,
	GUEST_RTV_W,
	LOCAL_DAY_HIKE_MODE1_E,
	LOCAL_DAY_HIKE_MODE2_E,
	TOUR_DAY_HIKE_MODE1_E,
	TOUR_DAY_HIKE_MODE2_E,
	INDEP_RTV_MODE1_E,
	INDEP_RTV_MODE2_E,
	GUEST_RTV_E;
//	TEST_PROFILE;
	
	
	// Original values 2017-11-06
//	THROUGH_TRAFFIC ( 0.02f, 900, 120 ),
//	BACK_COUNTRY    ( 0.01f, 600, 120 ),
//	LOCAL_DAY_HIKE  ( 0.15f, 600, 170 ),
//	TOUR_DAY_HIKE   ( 0.35f, 660, 180 ),
//	CYCLISTS        ( 0.01f, 660, 240 ),
//	GUEST_RTV       ( 0.05f, 540,  60 ),
//	INDEP_RTV       ( 0.41f, 780, 350 );
	
	private final int entryTimeMean;
	private final int entryTimeStdv;
	private final int numGenerate;
	private final float westPortion;
	private final int maxStops;
	private final Map<Destinations,ImmutablePair<Integer,Integer>> times;
	private final List<VisitList> routes;
	private final Map<Destinations,Float> routeProbabilities;
	private final Random r = new Random();
	
	private VisitorProfile()
	{
		Map<Destinations,ImmutablePair<Integer,Integer>> map = new HashMap<Destinations, ImmutablePair<Integer,Integer>>();
		routeProbabilities = new HashMap<Destinations,Float>();
		Properties props = new Properties();
		
		try
		{
			FileInputStream in = new FileInputStream( "/home/apcorn/gnpConfig/" + this.toString() + ".prop" );
			props.load( in );
			in.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
		this.numGenerate = Integer.parseInt( props.getProperty( "numGenerate" ) );
		this.westPortion = Float.parseFloat( props.getProperty( "westPortion" ) );
		this.maxStops = Integer.parseInt( props.getProperty( "maxStops" ) );
		this.entryTimeMean = Integer.parseInt( props.getProperty( "entryTime.mean" ) );
		this.entryTimeStdv = Integer.parseInt( props.getProperty( "entryTime.stdv" ) );
		
		for( Destinations d : Destinations.values() )
		{
			Integer mean;
			Integer stdv;
			
			if( props.getProperty( d.toString() + ".mean" ) == null || props.getProperty( d.toString() + ".stdv" ) == null )
			{
				System.err.println( "Warning: no entry in " + this + ".prop for node " + d + ". Defaulting to 0 mean and 0 stdv." );
				mean = 480;
				stdv = 60;
			}
			else
			{
				mean = Integer.parseInt( props.getProperty( d.toString() + ".mean" ) );
				stdv = Integer.parseInt( props.getProperty( d.toString() + ".stdv" ) );
			}
			
			map.put( d, new ImmutablePair<Integer,Integer>( mean, stdv ) );
			
			String key = d.toString() + ".turnaround.routeProb";
			if( props.getProperty( key ) != null )
			{
				Float probability = Float.parseFloat( props.getProperty( key ) );
				routeProbabilities.put( d, probability );
			}
		}
		
		routes = new ArrayList<VisitList>();
		int i = 0;
		
		for(;;)
		{
			String key = "route." + i;
			if( props.getProperty( key ) == null )
				break;
			
			List<Destinations> tempList = new ArrayList<Destinations>();
			String route = props.getProperty( key );
			for( String s : route.split( "," ) )
			{
				tempList.add( Destinations.valueOf( s ) );
			}
			
			Destinations turnaround = null;
			
			if( props.getProperty( key + ".turnaround" ) != null )
				turnaround = Destinations.valueOf( props.getProperty( key + ".turnaround" ) );
			
			routes.add( new VisitList( tempList, turnaround, i ) );
			
			i++;
		}
		
		this.times = map;
	}
	
	public int numGenerate()
	{
		return numGenerate;
	}
	
	public int getEntryTimeMean()
	{
		return entryTimeMean;
	}
	
	public int getEntryTimeStd()
	{
		return entryTimeStdv;
	}
	
	public int getParkTimeMean( Destinations d )
	{
		return times.get( d ).left.intValue();
	}
	
	public int getParkTimeStdv( Destinations d )
	{
		return times.get( d ).right.intValue();
	}
	
	public VisitList randomVisitList()
	{
		float f = r.nextFloat();
		float accum = 0f;
		
		Destinations turnaround = null;
		
		for( Destinations d : Destinations.values() )
		{
			if( routeProbabilities.get( d ) == null )
				continue;
			
			accum += routeProbabilities.get( d );
			if( f < accum )
			{
				turnaround = d;
				break;
			}
		}
		
		if( accum > 1.0001 )
		{
			// this is just a notice to double-check your inputs. easy to mistakenly put in more than 1.0, which would result in bad simulation outputs
			System.err.println( "ROUTE PROBABILITIES MAY TOTAL GREATER THAN 1.0 IN PROFILE " + this.toString() + " (just double check, floating point arithmetic is wonky so this warning could be wrong.)" );
			System.err.println( "turnaround is " + turnaround );
		}
		
		List<VisitList> tempRoutes = new ArrayList<VisitList>();
		
		for( VisitList vl : routes )
			if( vl.turnaround == turnaround )
				tempRoutes.add( vl );
		
		if( tempRoutes.size() == 0 )
			System.err.println( "No routes in " + this.toString() + " with turnaround " + turnaround );
		
		return tempRoutes.get( r.nextInt( tempRoutes.size() ) );
	}
	
	public int maxStops()
	{
		return maxStops;
	}
	
	public float westPortion()
	{
		return westPortion;
	}
	
	public float eastPortion()
	{
		return 1f - westPortion;
	}
}
