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
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.cmu.tepper.sme.gnp2.ParkingTier;

// this, and the visitor profiles, should not be enums but instead regular java types with all the special stuff stored in config files.
public enum Destinations
{
	WEST_LOT,
	WEST_GLACIER,        
	APGAR_TRANSIT,
	LAKE_MCDONALD_LODGE, 
	AVALANCH_CREEK,     
	LOOP,            
	OBERLIN_BEND,        
	LOGAN_PASS,          
	SIYEH_BEND,          
	GUNSIGHT,            
	ST_MARY_SHUTTLE,     
	SUNRIFT_GORGE,       
	SUN_POINT,           
	ST_MARY_CENTER,
	EAST_LOT;
	
	private final String name;
	private final SortedSet<ParkingTier> parkingTiers;
	private final SortedSet<ParkingTier> singleTier;
	private final SortedSet<ParkingTier> reverse;
	private final int totalParkingSpaces;
	private boolean ignoreTiers = false;
	private boolean adaptive = false;
	
	private Destinations()
	{
		parkingTiers = new TreeSet<ParkingTier>();
		Properties props = new Properties();
		
		try
		{
			FileInputStream in = new FileInputStream( "/home/apcorn/gnpConfig/" + this.name() + ".node" );
			props.load( in );
			in.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
		this.name = props.getProperty( "name" );
		
		int i = 0;
		int tps = 0;
		
		for(;;)
		{
			String keyCapacity = "parkingTier." + i + ".count";
			String keyTimeLimit = "parkingTier." + i + ".time";
			if( props.getProperty( keyCapacity ) == null || props.getProperty( keyTimeLimit ) == null )
				break;
			
			Integer capacity = Integer.parseInt( props.getProperty( keyCapacity ) );
			Integer timeLimit = Integer.parseInt( props.getProperty( keyTimeLimit ) );
			
			parkingTiers.add( new ParkingTier( i, capacity, timeLimit ) );
			
			i++;
			tps += capacity;
		}
		
		this.totalParkingSpaces = tps;
		
		reverse = new TreeSet<ParkingTier>( Collections.reverseOrder() );
		reverse.addAll( parkingTiers );
		
		singleTier = new TreeSet<ParkingTier>();
		ParkingTier pt = new ParkingTier( 0, totalParkingSpaces, 9999 );
		singleTier.add( pt );
	}
	
	public String getName()
	{
		return name;
	}
	
	public int totalParkingSpaces()
	{
		return totalParkingSpaces;
	}
	
	public SortedSet<ParkingTier> parkingTiers( VisitorProfile vp )
	{
		if( ignoreTiers )
			return singleTier;
		
		if( !adaptive )
			return parkingTiers;

		switch( vp )
		{
			case GUEST_RTV_E:
			case GUEST_RTV_W:
			case INDEP_RTV_MODE1_E:
			case INDEP_RTV_MODE1_W:
			case INDEP_RTV_MODE2_E:
			case INDEP_RTV_MODE2_W:
				return reverse;
			default:
				return parkingTiers;
		}
	}
	
	public SortedSet<ParkingTier> parkingTiersNoBs()
	{
		if( ignoreTiers )
			return singleTier;
		
		return parkingTiers;
	}
	
	public void ignoreTiers( boolean ignore )
	{
		ignoreTiers = ignore;
	}
	
	public void setAdaptive( boolean adaptive )
	{
		this.adaptive = adaptive;
	}
	
	public boolean isAdaptive( VisitorProfile vp )
	{
		switch( vp )
		{
			case GUEST_RTV_E:
			case GUEST_RTV_W:
			case INDEP_RTV_MODE1_E:
			case INDEP_RTV_MODE1_W:
			case INDEP_RTV_MODE2_E:
			case INDEP_RTV_MODE2_W:
				return adaptive;
			default:
				return false;
		}
	}
}
