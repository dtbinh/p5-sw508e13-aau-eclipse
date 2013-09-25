import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.remote.RemoteNXT;
import lejos.util.Delay;


public class RemoteNXTFunctions {
	RemoteNXT BottomNXT = null;
	private static final int yFactor = -345;
	private static final int xFactor = -300;
	private static final int zFactor = 230;
	private static final double displacementFactor = 3.2;
    private int presentY = (int)(-yFactor*2.75);
    private int presentX = 0;
    private TouchSensor TouchOnX;
    private TouchSensor TouchOnZ;
    private TouchSensor TouchOnY;
    private ColorSensor ColorSensorOnBoard;
    Board CheckersBoard;
    NXTMotor ZMotor;
	
	public RemoteNXTFunctions() throws InterruptedException, IOException{
		connect();
		BottomNXT.A.setSpeed(400);
		BottomNXT.B.setSpeed(400);
	    Motor.A.setSpeed(100);
	    Motor.B.setSpeed(1000);
	    BottomNXT.A.setAcceleration(1000);
	    BottomNXT.B.setAcceleration(1000);
	    Motor.A.setAcceleration(3000);
	    Motor.B.setAcceleration(3000);
	    TouchOnX = new TouchSensor(BottomNXT.S1);
	    TouchOnZ = new TouchSensor(SensorPort.S2);
	    TouchOnY = new TouchSensor(BottomNXT.S2);
	    ColorSensorOnBoard = new ColorSensor(SensorPort.S1);
	    ZMotor = new NXTMotor(MotorPort.C);
	    Reset();
	    CheckersBoard = new Board(this);
	}
	
	public ColorSensor.Color GetColorOnField (int x, int y) throws IOException{
		MoveSensorTo(x, y, false);
		
		return ColorSensorOnBoard.getColor();
	}
	
	public void MoveAndTakePiece(Field FromField, List<Field> FieldsToStopOnTheWay) throws IOException, InterruptedException
	{
		Field PresentField = FromField;
		Field TrashField = new Field();
		TrashField.y = -4;
		TrashField.x = 3;
		List<Field> TakenPieces = new ArrayList<Field>();
		
		for(int i = 0; i < FieldsToStopOnTheWay.size(); i++)
		{
			Field JumpedField = MovePieceOverField(PresentField,FieldsToStopOnTheWay.get(i));
			if(JumpedField != null){
				TakenPieces.add(JumpedField);
			}
			PresentField = FieldsToStopOnTheWay.get(i);
		}
		
		for(int i = 0; i < TakenPieces.size(); i++){
			MovePiece(TakenPieces.get(i), TrashField);
		}
	}
	
	private Field MovePieceOverField(Field FromField, Field ToField) throws IOException, InterruptedException{
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
		MoveTopTo(x);
		MoveButtomTo(y,GoToMagnet);

		BottomNXT.A.waitComplete();
		BottomNXT.B.waitComplete();
		Motor.B.waitComplete();
	}
	
	private void MoveTopTo(int x) throws IOException{
		Motor.B.rotate(x*xFactor-presentX, true);
		presentX = x*xFactor;
	}
	
	private void MoveButtomTo(int y, boolean GoToMagnet)
	{
		int displacement = 0;
		if(GoToMagnet == true){
			displacement =  (int) (yFactor*displacementFactor);
		}
		MoveBothAAndBMotor(y*yFactor-presentY+displacement);
		
		presentY = y*yFactor+displacement;
	}
	
	private void MovePiece(Field FromField, Field ToField) throws IOException, InterruptedException
	{
		MoveSensorTo(FromField.x,FromField.y,true);
		Motor.A.rotate(zFactor);
		ZMotor.setPower(100);
		Motor.A.rotate(-(zFactor/2));
		MoveSensorTo(ToField.x,ToField.y,true); 
		Motor.A.rotate(zFactor/2);
		ZMotor.setPower(0);
		Delay.msDelay(500);
		ResetZ();
		CheckersBoard.movePiece(FromField, ToField);
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

		while(!TouchOnX.isPressed() || !TouchOnZ.isPressed()|| !TouchOnY.isPressed())
		{
			if(TouchOnX.isPressed()){
				Motor.B.stop();
			}
			if(TouchOnZ.isPressed()){
				Motor.A.stop();
			}
			if(TouchOnY.isPressed()){
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
