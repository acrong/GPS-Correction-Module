package com.example.oss_client6;

/*
 * This Module Stabilizes GPS Error by 
 * calculating the average of GPS error ranges using displacement value from DisplacementIntegrationModule. 
 */
public class GPSErrorStabilizerModule {

	/*
	 *	Constructor.
	 *	The Displacement array should come from the DisplacementIntegrationModule. 
	 */
	public GPSErrorStabilizerModule() {
	}

	public GPSErrorStabilizerModule(float[][] Absolute_Displacement) {
		m_Absolute_Displacement = Absolute_Displacement;
	}

	public void setAbsoluteDisplacement(float[][] Absolute_Displacement)
	{
		m_Absolute_Displacement = Absolute_Displacement;
	}


	/*
	 * Input Value. 
	 * Gets the argument from any GPS source. 
	 */
	public void SetGPSValues(Location arg1)
	{
		if(myLocation == null)
		{
			myLocation.set(arg1.getLongitude(), arg1.getLatitude());
		}
		else
		{
			CheckToggleBaseCoordinate();
			StabilizeCoordinate();
		}
	}

	/*
	 * The result value. 
	 */
	public Location GetStabilizedLocation()
	{
		return StabilizedLocation;
	}

	/*
	 * The BaseCoordinate is constantly toggled every "DisplacementMaxAccuracyRange"m change. 
	 */
	private void CheckToggleBaseCoordinate()
	{
		if(currentBaseCoordinate == 0) // Initial try
		{
			if(getDistance(baseCoordinate1,  myLocation) > DisplacementRange)
			{
				currentBaseCoordinate = 1;
				for(int i=0; i<3; i++)
					m_Absolute_Displacement[0][i] = 0;
			}
			else
				StabilizedLocation.set(myLocation.getLongitude(), myLocation.getLatitude());
		}
		else if(currentBaseCoordinate == 1) // Toggles to BaseCoordinate 2 and resets BaseCoordinate 1
		{
			if(getDistance(baseCoordinate2, myLocation) > DisplacementRange)
			{
				currentBaseCoordinate = 2;
				baseCoordinate1.set(myLocation.getLongitude(), myLocation.getLatitude());
				for(int i=0; i<3; i++)
					m_Absolute_Displacement[0][i] = 0;
			}
		}
		else 							   // Toggles to BaseCoordinate 1 and resets BaseCoordinate 2
		{
			if(getDistance(baseCoordinate1, myLocation) > DisplacementRange)
			{
				currentBaseCoordinate = 1;
				baseCoordinate2.set(myLocation.getLongitude(), myLocation.getLatitude());
				for(int i=0; i<3; i++)
					m_Absolute_Displacement[1][i] = 0;
			}
		}
	}

	/*
	 * Finds the GPS Error range average between CurrentBaseCoordinate and CurrentLocation. 
	 */
	private void StabilizeCoordinate()
	{
		Location base;
		if(currentBaseCoordinate == 1)
			base = baseCoordinate1;
		else
			base = baseCoordinate2;

		// Changes Latitude Longitude to Meters
		double baseX = LattoMeter(base.getLatitude()); 
		double baseY = LongtoMeter(base.getLongitude());
		double currentX = LattoMeter(myLocation.getLatitude());
		double currentY = LongtoMeter(myLocation.getLongitude());
		// Calculates Error range Average of two points. 
		double resultX = AverageEquation(baseX, currentX, m_Absolute_Displacement[currentBaseCoordinate-1][0]);
		double resultY = AverageEquation(baseY, currentY, m_Absolute_Displacement[currentBaseCoordinate-1][1]);

		// Sets the result
		if(currentBaseCoordinate != 0)
			StabilizedLocation.set(MetertoLong(resultY), MetertoLat(resultX));

	}

	/*
	 * Current Coordinate's Error range is reset to Average Error range between the BaseCoordinate. 
	 */
	static double AverageEquation(double baseCoord, double targetCoord, double displacement)
	{
		return ((baseCoord + (targetCoord - displacement)) /2) + displacement;
	}

	/*
	 * Static Methods.
	 * The Constants in Converting Lat/Long to Meter are optimized for Korea.
	 */
	static double LattoMeter(double latitude)
	{

		int rad = (int)latitude;
		int min = (int)((latitude-rad)*60);
		double sec = (latitude - rad - (double)min/60)*3600;

		double  meter = ((rad*111) + (min*1.85) + (sec*0.031))*1000;
		return meter;
	}
	static double LongtoMeter(double longitude)
	{

		int rad = (int)longitude;
		int min = (int)((longitude-rad)*60);
		double sec = (longitude - rad - (double)min/60)*3600;

		double  meter = ((rad*88.8) + (min*1.48) + (sec*0.025))*1000;
		return meter;
	}

	static double MetertoLat(double meter)
	{
		double Kilometer = meter/1000;
		int rad = (int)(Kilometer/111);
		int min = (int)((Kilometer - 111*rad)/1.85);
		double sec = (Kilometer - 111*rad - 1.85*min)/0.31;

		double latitude = rad + (double)min/60 + sec/3600;
		return latitude;
	}

	static double MetertoLong(double meter)
	{
		double Kilometer = meter/1000;
		int rad = (int)(Kilometer/88.8);
		int min = (int)((Kilometer - 88.8*rad)/1.48);
		double sec = (Kilometer - 88.8*rad - 1.48*min)/0.025;

		double longitude = rad + (double)min/60 + sec/3600;
		return longitude;
	}

	static double getDistance(Location a, Location b)
	{
		double Distance = Math.sqrt(Math.pow((a.getLatitude() - b.getLatitude()),2) // uses Pythagorean Theorem.
				+ Math.pow((a.getLongitude() - b.getLongitude()),2));
		return Distance;
	}

	/*
	 * A Location class which contains the Latitude and Longitude values. 
	 */
	class Location
	{
		public Location()
		{
		}

		public Location(double Latitude, double Longitude)
		{
			m_Latitude = Latitude;
			m_Longitude = Longitude;
		}

		public void set(double Latitude, double Longitude)
		{
			m_Latitude = Latitude;
			m_Longitude = Longitude;
		}

		public void setLatitude(double Latitude)
		{
			m_Latitude = Latitude;
		}

		public void setLongitude(double Longitude)
		{
			m_Longitude = Longitude;
		}

		public double getLatitude()
		{
			return m_Latitude;
		}

		public double getLongitude()
		{
			return m_Longitude;
		}

		private double m_Latitude;
		private double m_Longitude;
	}

	/*
	 * The DisplacementIntegrationModule is not 100% accurate, so the Max Accuracy Range should be determined. 
	 * The Max Accuracy Range is 2 x DisplacementRange. 
	 * Also, the randomness of GPS Error Range are obtained by certain range of displacement. 
	 */
	public static final double DisplacementRange = 5;

	/*
	 * other variables. 
	 */
	float[][] m_Absolute_Displacement = null;
	Location myLocation = null;
	Location StabilizedLocation = null;
	Location baseCoordinate1 = null;
	Location baseCoordinate2 = null;
	int currentBaseCoordinate = 0;
}
