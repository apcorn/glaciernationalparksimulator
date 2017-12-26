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

// I've gone through and commented a lot of the code, but much is still undocumented. Please email me if you have questions or need more detailed explanations.
package edu.cmu.tepper.sme.gnp2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.cmu.tepper.sme.gnp2.enumeration.Destinations;

public class GlacierNationalParkModelTest
{
	public final String versionNum;
	public final boolean ignoreTiers;
	public final boolean adaptive;// allow visitors to park shorter than they'd like to
	
	public final String versionString;
	
	public static final int AVERAGE_SPEED = 30;
	public static final float AVERAGE_ROAD_TRAVERSAL_STDV = 0.25f;
	private VisitorSumpPump vsp;
	public final static String SUMP_PUMP_NAME = "VISITOR_EMIT_RETIRE";
	private List<ParkNode> parkableNodes = new LinkedList<ParkNode>();
	private List<ParkNode> roads = new LinkedList<ParkNode>();
	private BufferedWriter visitorWriter;
	private BufferedWriter nodeWriter;
	private BufferedWriter roadWriter;
//	private BufferedWriter configWriter;
	
	public GlacierNationalParkModelTest( String versionNum, boolean ignoreTiers, boolean adaptive )
	{
		this.versionNum = versionNum;
		this.adaptive = adaptive;
		this.ignoreTiers = ignoreTiers;
		this.versionString  = String.format( "%s_%s_%s", this.versionNum, ( this.ignoreTiers ? "no-tiers" : "tiers" ), this.adaptive ? "adaptive" : "non-adaptive" );
		
		for( Destinations d : Destinations.values() )
		{
			d.ignoreTiers( ignoreTiers );
			d.setAdaptive( adaptive );
		}
		
		try
		{
			// NOTE this should be changed to work on your platform and with your home directory
			visitorWriter = new BufferedWriter(new FileWriter( "/home/apcorn/gnp_" + versionString + "_visitor.csv" ));
			nodeWriter = new BufferedWriter(new FileWriter( "/home/apcorn/gnp_" + versionString + "_node.csv" ));
			roadWriter = new BufferedWriter(new FileWriter( "/home/apcorn/gnp_" + versionString + "_road.csv" ));
			init();
		}
		catch( IOException e )
		{
			e.printStackTrace();
			visitorWriter = null;
			System.exit( 1 );
		}
	}
	
	private void init()
	{
		// a future version would put all this stuff into a config file.
		//															   milemarker, traverse_mean traverse_std, destination enum, lat, lon
		ParkNode westAuxLot        = new ParkNode(  0, 5, 2, Destinations.WEST_LOT,        48.4950 , -113.9811 );
		ParkNode westGlacier       = new ParkNode(  1, 5, 2, Destinations.WEST_GLACIER,        48.4950 , -113.9811 );
		ParkNode apgarTransit      = new ParkNode(  3, 5, 2, Destinations.APGAR_TRANSIT,       48.5228 , -113.9891 );
		ParkNode lakeMcDonaldLodge = new ParkNode( 11, 5, 2, Destinations.LAKE_MCDONALD_LODGE, 48.6174 , -113.8791 );
		ParkNode avalancheCreek    = new ParkNode( 16, 5, 2, Destinations.AVALANCH_CREEK,      48.6780 , -113.8188 );
		ParkNode loop              = new ParkNode( 25, 5, 2, Destinations.LOOP,                48.7549 , -113.8003 );
		ParkNode oberlinBend       = new ParkNode( 31, 5, 2, Destinations.OBERLIN_BEND,        48.6994 , -113.7251 );
		ParkNode loganPass         = new ParkNode( 32, 5, 2, Destinations.LOGAN_PASS,          48.6966 , -113.7182 );
		ParkNode siyehBend         = new ParkNode( 34, 5, 2, Destinations.SIYEH_BEND,          48.7017 , -113.6679 );
		ParkNode gunsight          = new ParkNode( 36, 5, 2, Destinations.GUNSIGHT,            48.6777 , -113.6528 );
		ParkNode stMaryShuttle     = new ParkNode( 38, 5, 2, Destinations.ST_MARY_SHUTTLE,     48.6743 , -113.6087 );
		ParkNode sunriftGorge      = new ParkNode( 39, 5, 2, Destinations.SUNRIFT_GORGE,       48.6790 , -113.5956 );
		ParkNode sunPoint          = new ParkNode( 40, 5, 2, Destinations.SUN_POINT,           48.6758 , -113.5803 );
		ParkNode stMaryCenter      = new ParkNode( 50, 5, 2, Destinations.ST_MARY_CENTER,      48.7473 , -113.4391 );
		ParkNode eastAuxLot        = new ParkNode( 50, 5, 2, Destinations.EAST_LOT,        48.7473 , -113.4391 );

//		parkableNodes.add( westGlacier );
		parkableNodes.add( westAuxLot );
		parkableNodes.add( apgarTransit );
		parkableNodes.add( lakeMcDonaldLodge );
		parkableNodes.add( avalancheCreek );
		parkableNodes.add( loop );
		parkableNodes.add( oberlinBend );
		parkableNodes.add( loganPass );
		parkableNodes.add( siyehBend );
		parkableNodes.add( gunsight );
		parkableNodes.add( stMaryShuttle );
		parkableNodes.add( sunriftGorge );
		parkableNodes.add( sunPoint );
		parkableNodes.add( stMaryCenter );
		parkableNodes.add( eastAuxLot );
		
		vsp = new VisitorSumpPump();
		
		connect( westAuxLot, westGlacier );
		connect( westGlacier, apgarTransit );
		connect( apgarTransit, lakeMcDonaldLodge );
		connect( lakeMcDonaldLodge, avalancheCreek );
		connect( avalancheCreek, loop );
		connect( loop, oberlinBend );
		connect( oberlinBend, loganPass );
		connect( loganPass, siyehBend );
		connect( siyehBend, gunsight );
		connect( gunsight, stMaryShuttle );
		connect( stMaryShuttle, sunriftGorge );
		connect( sunriftGorge, sunPoint );
		connect( sunPoint, stMaryCenter );
		connect( stMaryCenter, eastAuxLot );
		
		directConnect( eastAuxLot, vsp );
		directConnect( vsp, westAuxLot );
		
		vsp.init(  );
	}
	
	private void run()
	{
		
		try
		{
			int length = 1440;// one whole day
			int nodeUpdateInterval = 15;
			
			// print header for node csv
			nodeWriter.write( "location,time,park_attempts,park_successes,visitors_parked,parking_used_percentage,latitude,longitude,visitors_traversing,park_tiers\n" );
			roadWriter.write( "location,time,park_attempts,park_successes,visitors_parked,parking_used_percentage,latitude,longitude,visitors_traversing,park_tiers\n" );
			
			for( int i = 0; i < length; i++ )
			{
				vsp.update( i );
				if( i % nodeUpdateInterval == 0 )
				{
					for( ParkNode pn : parkableNodes )
					{
						pn.dump( i, nodeWriter );
					}
					
					for( ParkNode pn : roads )
					{
						pn.dump( i, roadWriter );
					}
				}
			}
			
			System.out.println( "Simulation finished." );
			vsp.dump( 0, visitorWriter );
			
			visitorWriter.close();
			nodeWriter.close();
			roadWriter.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

	}
	
	public static SortedSet<ParkInfrastructure> buildParkableNodeList( ParkInfrastructure currentLocation )
	{
		SortedSet<ParkInfrastructure> parkableNodes = new TreeSet<ParkInfrastructure>();
		
		ParkInfrastructure temp = currentLocation.getEast();
		
		if( currentLocation.isParkable() )
			parkableNodes.add( currentLocation );
		
		while( !temp.equals( currentLocation ) )
		{
			if( temp.isParkable() )
				parkableNodes.add( temp );
			temp = temp.getEast();
		}
		
		return parkableNodes;
	}
	
	public List<ParkNode> getParkableNodes()
	{
		return parkableNodes;
	}
	
	private void connect( ParkInfrastructure a, ParkInfrastructure b )
	{
		int distance = Math.abs( ((ParkNode)a).mileMarker - ((ParkNode)b).mileMarker);
		float traverseTime = (float)distance / (float)AVERAGE_SPEED * 60f; 
		// Connect a (west) to b (east) via a road (road)
		ParkNode road = new ParkNode( "ROAD " + a.toString() + " to " + b.toString(), -1, (int)traverseTime, (int)(traverseTime * AVERAGE_ROAD_TRAVERSAL_STDV), null, 0.0, 0.0 );
		
		a.setEast( road );
		b.setWest( road );
		road.setEast( b );
		road.setWest( a );
		
		roads.add( road );
	}
	
	private void directConnect( ParkInfrastructure a, ParkInfrastructure b )
	{
		a.setEast( b );
		b.setWest( a );
	}
	
	public static void main( String [] args )
	{
		// this string makes the output files easier for people (data analysts) to read
		String versionString = "056-noHiker-noMeter-noRedirect-adaptive-";
		GlacierNationalParkModelTest gnpmt;
		int count = 100;
		
		for( int i = 0; i < count; i++ )
		{
			String localVersionString = String.format( "%s%02d", versionString, i );
//			gnpmt = new GlacierNationalParkModelTest( localVersionString, false, false );//#2 don't ignore tiers, don't be adaptive
//			gnpmt.run();
//			gnpmt = new GlacierNationalParkModelTest( localVersionString, false, true );//#5 don't ignore tiers, be adaptive
//			gnpmt.run();
			gnpmt = new GlacierNationalParkModelTest( localVersionString, true, false );
			gnpmt.run();
		}
		System.out.println( "End." );
	}
}
