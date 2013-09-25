import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.BasicMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.TouchSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.remote.FirmwareInfo;
import lejos.nxt.remote.RemoteNXT;
import lejos.util.Delay;


public class RemoteNXTFunctions {
	RemoteNXT BottomNXT = null;
	private static final int xFactor = -340;
	private static final int yFactor = -310;
	private static final int zFactor = 330;
	private static final int displacementFactor = 4;
    private int PresentX = (int)(-xFactor*2.75);
    private int PresentY = 0;
    private TouchSensor TouchOnY;
    private TouchSensor TouchOnZ;
    private TouchSensor TouchOnX;
    private ColorSensor ColorSensorOnBoard;
	
	public RemoteNXTFunctions() throws InterruptedException{
		connect();
		BottomNXT.A.setSpeed(400);
		BottomNXT.B.setSpeed(400);
	    Motor.A.setSpeed(100);
	    Motor.B.setSpeed(1000);
	    BottomNXT.A.setAcceleration(1000);
	    BottomNXT.B.setAcceleration(1000);
	    Motor.A.setAcceleration(3000);
	    Motor.B.setAcceleration(3000);
	    TouchOnY = new TouchSensor(BottomNXT.S1);
	    TouchOnZ = new TouchSensor(SensorPort.S2);
	    TouchOnX = new TouchSensor(BottomNXT.S2);
	    ColorSensorOnBoard = new ColorSensor(SensorPort.S1);
	    Reset();
	}
	
	public ColorSensor.Color GetColorOnField (int x, int y) throws IOException{
		MoveSensorTo(x, y, false);
		
		return ColorSensorOnBoard.getColor();
	}
	
	public void MoveAndTakePiece(Field FromField, Field ToField, List<Field> FieldsToStopOnTheWay)
	{
		Field PresentField = FromField;
		List<Field> TakenPieces = new ArrayList<Field>();
		
		if(FieldsToStopOnTheWay == null){
			FieldsToStopOnTheWay = new ArrayList<Field>();
		}
		FieldsToStopOnTheWay.add(ToField);
		
		for(int i = 0; i < FieldsToStopOnTheWay.size(); i++)
		{
			//TakenPieces.add(MovePieceOverField(PresentField,FieldsToStopOnTheWay.get(i)));
			PresentField = FieldsToStopOnTheWay.get(i);
		}
	}
	
	private Field MovePieceOverField(Field FromField, Field ToField) throws IOException{
		MovePiece(FromField, ToField);
		
		if(Math.abs(FromField.x - ToField.x) == 2){
			Field ReturnField = new Field();
			ReturnField.x = (FromField.x + ToField.x)/2;
			ReturnField.y = (FromField.y + ToField.y)/2;
			return ReturnField;
		}
		else
		{
			return null;
		}
	}
	
	private void MoveSensorTo(int x, int y, boolean GoToMagnet) throws IOException
	{
		MoveTopTo(y);
		MoveButtomTo(x,GoToMagnet);

		BottomNXT.A.waitComplete();
		BottomNXT.B.waitComplete();
		Motor.B.waitComplete();
	}
	
	private void MoveTopTo(int y) throws IOException{
		Motor.B.rotate(y*yFactor-PresentY, true);
		PresentY = y*yFactor;
	}
	
	private void MoveButtomTo(int x, boolean GoToMagnet)
	{
		int displacement = 0;
		if(GoToMagnet == true){
			displacement =  (int) (xFactor*displacementFactor);
		}
		MoveBothAAndBMotor(x*xFactor-PresentX+displacement);
		
		PresentX = x*xFactor+displacement;
	}
	
	private void MovePiece(Field Fromfield, Field Tofield) throws IOException
	{
		MoveSensorTo(Fromfield.x,Fromfield.y,true);
		Motor.A.rotate(zFactor);
		Motor.C.forward();
		Motor.A.rotate(-(zFactor/2));
		MoveSensorTo(Tofield.x,Tofield.y,true);
		Motor.A.rotate(zFactor/2);
		Motor.C.stop();
		Delay.msDelay(500);
		ResetZ();
		
	}
	
	
	private void MoveBothAAndBMotor(int angle){
		BottomNXT.A.rotate(angle, true);
		BottomNXT.B.rotate(angle,true);
	}
	
	private void ResetZ(){
		Motor.A.backward();
		while(!TouchOnZ.isPressed()){
			if(TouchOnZ.isPressed()){
				Motor.A.stop();
			}
		}
	}
	private void Reset(){
		Motor.B.setSpeed(200);
		Motor.A.backward();
		Motor.B.forward();
		BottomNXT.A.forward();
		BottomNXT.B.forward();

		while(!TouchOnY.isPressed() || !TouchOnZ.isPressed()|| !TouchOnX.isPressed())
		{
			if(TouchOnY.isPressed()){
				Motor.B.stop();
			}
			if(TouchOnZ.isPressed()){
				Motor.A.stop();
			}
			if(TouchOnX.isPressed()){
				BottomNXT.A.stop();
				BottomNXT.B.stop();
			}
		}
		Motor.B.stop();
		Motor.A.stop();
		BottomNXT.A.stop();
		BottomNXT.B.stop();
		Motor.B.setSpeed(1000);
	}
	
	private void connect() throws InterruptedException{
		// Now connect
	    try {
	        LCD.clear();
	        LCD.drawString("Connecting...",0,0);
	    	BottomNXT = new RemoteNXT("CheckBottom", Bluetooth.getConnector());
	    	LCD.clear();
	        LCD.drawString("Connected",0,1);
	        Thread.sleep(2000);
	    } catch (IOException ioe) {
	    	LCD.clear();
	        LCD.drawString("Conn Failed",0,0);
	        Thread.sleep(2000);
	        System.exit(1);
	    }
	}
}
