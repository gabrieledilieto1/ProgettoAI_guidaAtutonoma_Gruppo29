package scr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;


public final class SimpleDriver extends Controller {

	private Action action;
	private char ch = ' ';
	private boolean lettura = false;
	private double[] features = new double[8];

	private double angolo;

	
	private int classe = -1;
	private String firstLineOfTheFile = "Speed; DistanzaLineaCentrale; SensoreSX1; SensoreSX2; SensoreCentrale; SensoreDX1; SensoreDX2; Angolo; Classe\n";
	private File file;
	private NearestNeighbor nn;
	private String prototypes_filename;
	private boolean guidaAutonoma;

	
	private int recoveryCounter = 0;
	private int stuckCounter = 0;
	private int recoveryTime = 0;
	private boolean inRecovery = false;


	
	final int[] gearUp = {7500, 8000, 8500, 9000, 9500, 0};//quando bisogna cambiare marcia
	final int[] gearDown = {0, 2500, 3000, 3500, 4000, 4500};

	
	final int stuckTime = 25;
	final double stuckAngle = (double) 0.523598775; 

	
	final double maxSpeedDist = 70;
	final double maxSpeed = 150;
	final double sin5 = (double) 0.08716;
	final double cos5 = (double) 0.99619;

	
	final double steerLock = (double) 0.785398;
	final double steerSensitivityOffset = (double) 100.0;
	final double wheelSensitivityCoeff = 1.5;

	
	final double wheelRadius[] = {(double) 0.3179, (double) 0.3179, (double) 0.3276, (double) 0.3276};
	final double absSlip = (double) 1.5;
	final double absRange = (double) 2.5;
	final double absMinSpeed = (double) 5.0;


	final double clutchMax = (double) 0.4;
	final double clutchDelta = (double) 0.04;
	final double clutchRange = (double) 0.85;
	final double clutchDeltaTime = (double) 0.02;
	final double clutchDeltaRaced = 10;
	final double clutchDec = (double) 0.01;
	final double clutchMaxModifier = (double) 1.3;
	final double clutchMaxTime = (double) 1.5;

	private int stuck = 0;

	
	private double clutch = 0;

		public SimpleDriver() {
		action = new Action();
		int scelta = JOptionPane.showConfirmDialog(null, "Vuoi abilitare la guida autonoma?", "Seleziona modalità",
				JOptionPane.YES_NO_OPTION);

		this.guidaAutonoma = (scelta == JOptionPane.YES_OPTION);

		if (!guidaAutonoma) {
			this.file = new File("dataset.csv");
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
				bw.append(firstLineOfTheFile);
			} catch (IOException ex) {
			}
			SwingUtilities.invokeLater(() -> new CharReader(this));
		} else {
			prototypes_filename = "dataset.csv";
			this.nn = new NearestNeighbor(prototypes_filename);
		}

	}

	
	public char getCh() {
		return ch;
	}


	public boolean isLettura() {
		return lettura;
	}

	
	public void setLettura(boolean lettura) {
		this.lettura = lettura;
	}

	
	public void setCh(char ch) {
		this.ch = ch;
	}

	

	@Override
	public void reset() {
		System.out.println("Restarting the race!");
	}

	
	@Override
	public void shutdown() {
		System.out.println("Bye bye!");
	}

	
	public int getGear(SensorModel sensors) {
		int gear = sensors.getGear();
		double rpm = sensors.getRPM();

		
		if (gear < 1) {
			return 1;
		}
		
		if (gear < 6 && rpm >= gearUp[gear - 1]) {
			return gear + 1;
		} else 
			
			if (gear > 1 && rpm <= gearDown[gear - 1]) {
				return gear - 1;
			} else 
			{
				return gear;
			}
	}

	


	public double normalizzatoreMinMax(double data, double min, double max) {
		double x = (data - min) / (max - min);
		return x;
	}

	@Override
	public Action control(SensorModel sensors) {

		//input
		features[0] = normalizzatoreMinMax(sensors.getSpeed(), 0.0, 280);
		features[1] = normalizzatoreMinMax(sensors.getTrackPosition(), -10.0, 10.0);
		features[2] = normalizzatoreMinMax(sensors.getTrackEdgeSensors()[3], -1.0, 200);
		features[3] = normalizzatoreMinMax(sensors.getTrackEdgeSensors()[6], -1.0, 200);
		features[4] = normalizzatoreMinMax(sensors.getTrackEdgeSensors()[9], -1.0, 200);
		features[5] = normalizzatoreMinMax(sensors.getTrackEdgeSensors()[12], -1.0, 200);
		features[6] = normalizzatoreMinMax(sensors.getTrackEdgeSensors()[15], -1.0, 200);
		features[7] = normalizzatoreMinMax(sensors.getAngleToTrackAxis(), -Math.PI, Math.PI);

		angolo = sensors.getAngleToTrackAxis();

		if (!guidaAutonoma) {
			try {
				writeCSV();
			} catch (Exception ex) {
				System.out.println("Errore accesso CSV");
			}
		} else {
			predictControl(); 

			
			System.out.println("*** SENSOR DATA ***");
			System.out.println("Speed: " + sensors.getSpeed());
			System.out.println("Track Position: " + sensors.getTrackPosition());
			System.out.println("Angle to Axis: " + sensors.getAngleToTrackAxis());
			System.out.println("Sensori SX1: " + sensors.getTrackEdgeSensors()[3]);
			System.out.println("Sensori SX2: " + sensors.getTrackEdgeSensors()[6]);
			System.out.println("Sensore Centrale: " + sensors.getTrackEdgeSensors()[9]);
			System.out.println("Sensori DX1: " + sensors.getTrackEdgeSensors()[12]);
			System.out.println("Sensori DX2: " + sensors.getTrackEdgeSensors()[15]);
			System.out.println("Classe predetta dal Nearest Neighbor: " + classe);
			System.out.println("***************************");
		}

		autoControl(); // esegue la classe

		if (action.gear >= 0) {
			action.gear = getGear(sensors);
		}

		action.brake = filterABS(sensors, action.brake);
		action.clutch = clutching(sensors, action.clutch);

		return action;
	}



	private double filterABS(SensorModel sensors, double brake) {
		
		double speed = (double) (sensors.getSpeed() / 3.6);
		
		if (speed < absMinSpeed) {
			return brake;
		}

		
		double slip = 0.0f;
		for (int i = 0; i < 4; i++) {
			slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
		}
		
		slip = speed - slip / 4.0f;
		
		if (slip > absSlip) {
			brake = brake - (slip - absSlip) / absRange;
		}

		
		if (brake < 0) {
			return 0;
		} else {
			return brake;
		}
	}

	
	public double clutching(SensorModel sensors, double clutch) {

		double maxClutch = clutchMax;

		
		if (sensors.getCurrentLapTime() < clutchDeltaTime && getStage() == Stage.RACE
				&& sensors.getDistanceRaced() < clutchDeltaRaced) {
			clutch = maxClutch;
		}

		
		if (clutch > 0) {
			double delta = clutchDelta;
			if (sensors.getGear() < 2) {
				
				delta /= 2;
				maxClutch *= clutchMaxModifier;
				if (sensors.getCurrentLapTime() < clutchMaxTime) {
					clutch = maxClutch;
				}
			}

			
			clutch = Math.min(maxClutch, clutch);

			
			if (clutch != maxClutch) {
				clutch -= delta;
				clutch = Math.max((double) 0.0, clutch);
			}
			else {
				clutch -= clutchDec;
			}
		}
		return clutch;
	}

	
	@Override
	public float[] initAngles() {

		float[] angles = new float[19];

		/*
		 * setta gli angoli aq:
		 * {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90}
		 */
		for (int i = 0; i < 5; i++) {
			angles[i] = -90 + i * 15;
			angles[18 - i] = 90 - i * 15;
		}

		for (int i = 5; i < 9; i++) {
			angles[i] = -20 + (i - 5) * 5;
			angles[18 - i] = 20 - (i - 5) * 5;
		}
		angles[9] = 0;
		return angles;
	}

    /*
    Elenco Classi
    0 -> accelera
    1 -> giraSXMolto
    2 -> giraSX
    3 -> giraSXPoco
    4 -> giraDXMolto
    5 -> giraDX
    6 -> giraDXPoco
    7 -> frena
    8 -> retromarcia
    9 -> decelera
    */



	public void writeCSV() throws Exception {

		System.out.println("Scrittura CSV: ch = " + ch + " | lettura = " + lettura);


		if (ch == 'w') {
			classe = 0;
		} else if (ch == 'a' && features[4] < 0.2 && features[0] > 0.5) {
			classe = 1;
		} else if (ch == 'a' && features[4] < 0.5) {
			classe = 2;
		} else if (ch == 'a') {
			classe = 3;
		} else if (ch == 'd' && features[4] < 0.2  && features[0] > 0.5) {
			classe = 4;
		} else if (ch == 'd' && features[4] < 0.5) {
			classe = 5;
		} else if (ch == 'd') {
			classe = 6;
		} else if (ch == 's') {
			classe = 7;
		} else if (ch == 'r') {
			classe = 8;
		} else {
			classe = 9;
		}

		if (lettura) {
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {

				System.out.println("Scrivo riga con classe: " + classe);


				bw.append(features[0] + "; ");
				bw.append(features[1] + "; ");
				bw.append(features[2] + "; ");
				bw.append(features[3] + "; ");
				bw.append(features[4] + "; ");
				bw.append(features[5] + "; ");
				bw.append(features[6] + "; ");
				bw.append(features[7] + "; ");
				bw.append(String.valueOf(classe) + '\n');

			}
		}
	}

		public void predictControl() {

		Sample point = new Sample(features);

		int predictedClass = nn.classify(point);

		classe = predictedClass;
	}

	
	public void autoControl() {

		switch(classe) {
			case 0:
				accelera();
				break;
			case 1:
				giraSXMolto();
				break;
			case 2:
				giraSX();
				break;
			case 3:
				giraSXPoco();
				break;
			case 4:
				giraDXMolto();
				break;
			case 5:
				giraDX();
				break;
			case 6:
				giraDXPoco();
				break;
			case 7:
				frena();
				break;
			case 8:
				retromarcia();
				break;
			default:
				decelera();
				break;

		}

	}

	private void accelera() {
		if (action.gear == -1) {
			action.gear = 1;
		}
		action.steering = 0;
		action.brake = 0;
		action.accelerate = 1;
	}

	private void giraSXMolto() {
		action.accelerate = 0;
		action.brake = 1;
		action.steering = 0.5;
	}

	private void giraSX() {
		action.accelerate = 0.5;
		action.brake = 0;
		action.steering = 0.25;
	}

	private void giraSXPoco() {
		action.accelerate = 1;
		action.brake = 0;
		action.steering = 0.1;
	}

	private void giraDXMolto() {
		action.accelerate = 0;
		action.brake = 1;
		action.steering = -0.5;
	}

	private void giraDX() {
		action.accelerate = 0.5;
		action.brake = 0;
		action.steering = -0.25;
	}

	private void giraDXPoco() {
		action.accelerate = 1;
		action.brake = 0;
		action.steering = -0.1;
	}

	private void frena() {
		action.steering = 0;
		action.accelerate = 0;
		action.brake = 1;
	}
	private void retromarcia() {
		action.gear = -1;
		action.brake = 0;
		action.accelerate = 0.5;
		action.steering = 0;
	}


	private void decelera() {
		if (action.gear == -1) {
			action.gear = 1;
		}
		action.accelerate = 0;
		action.brake = 0;
		action.steering = 0;
	}

}
