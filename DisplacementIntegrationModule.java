package com.example.oss_client6;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/*
 * This Module gets how much the device has moved in two steps.
 * 1. Changes the device's coordinate system based acceleration values into world's.
 * 2. Integrates the acceleration value twice to get the displacement. 
 */
public class DisplacementIntegrationModule {

	/*
	 *  Constructor
	 */
	public DisplacementIntegrationModule() {
		m_Linear_Acceleration[3] = 1;
		m_Temp_Absolute_Acceleration[3] = 0;
		m_Temp_Absolute_Velocity[3] = 0;
		m_Absolute_Displacement[1][3] = 0;
		m_Absolute_Displacement[0][3] = 0;
		m_Absolute_Velocity[3] = 0;
		m_time = System.currentTimeMillis();

	}

	/*
	 * Getters
	 */
	public float[][] GetAbsoluteDisplacement()
	{
		return m_Absolute_Displacement;
	}

	public float[] GetAbsoluteVelocity()
	{
		return m_Absolute_Velocity;
	}

	public float[] GetAbsoluteAcceleration()
	{
		return m_Absolute_Acceleration;
	}

	/*
	 * Input Value.
	 * Gets the argument from "SensorEventListener"'s "onSensorChanged" Method
	 */
	public void SetSensorValues(SensorEvent event)
	{
		SensorThread Thread = new SensorThread(event);
		Thread.start();
	}

	/*
	 * Thread in which calculation is done
	 */
	class SensorThread extends Thread
	{
		SensorEvent mEvent;

		public SensorThread(SensorEvent event)
		{
			mEvent = event; 
		}

		public void run()
		{
			switch(mEvent.sensor.getType())
			{
			//Puts Gravity vector value
			case Sensor.TYPE_GRAVITY: 
			{						
				for(int i=0; i<3; i++) 
				{							
					m_Gravity[i] = mEvent.values[i]; 							
				}
				break;
			}

			//Puts Linear Acceleration vector value
			case Sensor.TYPE_LINEAR_ACCELERATION:
			{ 																		
				for(int i=0; i<3; i++)
				{																	
					m_Linear_Acceleration[i] = mEvent.values[i]; 																		
				}
				if(m_Gravity[2] != 0)
				{
					ready = true;

				}


				if(ready) { // if all variables for calculation are prepared

					SensorManager.getRotationMatrix(m_RotationMatrix, null, m_Gravity, null); // Gets the rotation matrix of the device.

					android.opengl.Matrix.invertM(m_InvertedRotationMatrix,0,m_RotationMatrix,0); // Inverts the rotation matrix to multiply
					android.opengl.Matrix.multiplyMV(m_Absolute_Acceleration,0,m_InvertedRotationMatrix,0,m_Linear_Acceleration,0);
					// Multiplies the rotation matrix  to linear acceleration vector to change it from device's coordinate system to
					// the world's coordinate system. 
					getDisplacement(); //integrates the acceleration value to get velocity and displacement.
				}

				break;
			}
			}
		}

	}

	/*
	 * Integrating the acceleration value twice to get displacement.
	 */
	private void getDisplacement()
	{
		// to calculate the time interval between acceleration values. 
		doTimeCount();

		if(m_TimeConstant != 0.0f)
		{
			// Integrating acceleration to velocity
			if(m_Temp_Absolute_Acceleration[3] == 1)
			{
				for(int i=0; i<3; i++)
				{
					// Caculating the area of trapezoid. (a+b)*h * 1/2
					m_Absolute_Velocity[i] +=( m_TimeConstant * (m_Temp_Absolute_Acceleration[i] + m_Absolute_Acceleration[i])) / 2.0f;

				}
				m_Absolute_Velocity[3] = 1;
			}

			// Integrating velocity to displacement	
			if(m_Temp_Absolute_Velocity[3] ==1)
			{
				for(int i=0; i<3; i++)
				{
					for(int k=0; k<2; k++)
						// Caculating the area of trapezoid. (a+b)*h * 1/2
						m_Absolute_Displacement[k][i] += ( m_TimeConstant * (m_Temp_Absolute_Velocity[i] + m_Absolute_Velocity[i])) / 2.0f;
				}
			}

			for(int i=0; i<4; i++)
			{
				m_Temp_Absolute_Acceleration[i] = m_Absolute_Acceleration[i];
				m_Temp_Absolute_Velocity[i] = m_Absolute_Velocity[i];
			}
		}

	}

	/*
	 * Calculates the time interval between sensor value inputs
	 */
	private void doTimeCount()
	{
		if(m_time + 1000 < System.currentTimeMillis())
		{
			isCount = true;
			m_time = System.currentTimeMillis();
		}
		else
		{
			isCount = false;
		}

		m_Count++;

		if(isCount)
		{
			m_TickCount = m_Count;
			m_Count = 0;

			m_TimeConstant = (float)1 / (float)m_TickCount;
		}
	}



	// Vectors and Matrixes 
	private float[] m_Gravity = new float[3];
	private float[] m_RotationMatrix = new float[16];
	private float[] m_InvertedRotationMatrix = new float[16];
	private float[] m_Linear_Acceleration = new float[4];		// device's coordinate acceleration vector
	private float[] m_Absolute_Acceleration = new float[4];		// world's coordinate acceleration vector
	private float[] m_Temp_Absolute_Acceleration = new float[4]; // used in integration
	private float[] m_Temp_Absolute_Velocity = new float[4];	// used in integration
	private float[] m_Absolute_Velocity = new float[4];
	private float[][] m_Absolute_Displacement = new float[2][4]; // the array's 1st dimension is for GPSErrorStabilizerModule

	// other variables
	private float m_TimeConstant = 0.0f;
	private int m_Count = 0;
	private int m_TickCount = 0;
	private long m_time = 0;
	private boolean isCount = false;
	private boolean ready = false;

}
