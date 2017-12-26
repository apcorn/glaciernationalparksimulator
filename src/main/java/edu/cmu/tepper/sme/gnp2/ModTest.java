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

public class ModTest
{
// just testing my gnprandom class. it works. this class is otherwise unused in the simulator
	public static void main( String [] args )
	{
		GnpRandom r = new GnpRandom();
		
		for( int i = 0; i < 1000; i++ )
		{
			System.out.format( "%05d%n", r.norm( 1, 0 ) );
		}
	}
}
