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

import java.util.List;
import java.util.Map;

import edu.cmu.tepper.sme.gnp2.enumeration.Destinations;

public class VisitList
{
	public final int routeId;
	public final List<Destinations> list;
	public final Destinations turnaround;
	
	public VisitList( List<Destinations> list, Destinations turnaround, int routeId )
	{
		this.list = list;
		this.turnaround = turnaround;
		this.routeId = routeId;
	}
}
