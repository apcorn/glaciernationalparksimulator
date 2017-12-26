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

import org.apache.commons.lang3.tuple.ImmutablePair;

import edu.cmu.tepper.sme.gnp2.enumeration.Destinations;

public interface ParkInfrastructure
{
	public void setEast( ParkInfrastructure r );
	public void setWest( ParkInfrastructure r );
	public ParkInfrastructure getEast();
	public ParkInfrastructure getWest();
	public boolean isParkable();
	public boolean park( Visitor v );
	public void leave( Visitor v );
	public int traversalTime();
	public void dump( int time, BufferedWriter bw );
	public ImmutablePair<Double, Double> getLatLong();
	public Destinations getDestination();
	public void traverse( Visitor v );
	public void endTraverse( Visitor v );
	public boolean hasParking();
}
