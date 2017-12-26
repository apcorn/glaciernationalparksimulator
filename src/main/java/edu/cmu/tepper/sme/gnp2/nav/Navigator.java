package edu.cmu.tepper.sme.gnp2.nav;

import edu.cmu.tepper.sme.gnp2.Heading;

public abstract class Navigator
{
	public abstract Heading getHeading();
	public abstract void move( int time );
	
	public final void endNav()
	{
		
	}
}
