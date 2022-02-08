import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.kristou.urgLibJ.Connection.EthernetConnection;
import com.kristou.urgLibJ.RangeSensor.RangeSensorInformation;
import com.kristou.urgLibJ.RangeSensor.UrgDevice;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureData;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureSettings;

public class MainFromRadar extends Thread{
	private static final boolean ENABLE_RADAR = true;
	JFrame frame;
	View view;
	Data data;
	Read read;
	RadarReader radarReader;
	JSlider sliderOfAccuracy;
	JSlider sliderOfDivisor;
	JSlider sliderOfPowerOfMaxGroup;
	JSlider sliderOfPowerOfVariance;
	JSlider sliderOfScale;
	JSlider sliderOfAngle;
	JSlider sliderOfShift;
	JSlider sliderOfMinimumX;
	JSlider sliderOfMinimumY;
	JSlider sliderOfMaximumX;
	JSlider sliderOfMaximumY;
	JCheckBox checkboxMouse;


	// minThreshold=maxDepth/divisor
	int shift = 50;
	int divisor = 10;
	int accuracy = 100;
	int powerOfMaxGroup = 0;
	int powerOfVariance = 100;
	int angle=360;
	int scale=120;
	int minimumX = 0;
	int minimumY = 0;
	int maximumX = View.DEFAULT_SIZE;
	int maximumY = View.DEFAULT_SIZE;
	boolean mouse = false;
	boolean start = false;
	String file="1.txt";

	
	public static void main(String[] args) throws InterruptedException, IOException, AWTException {
		MainFromRadar main= new MainFromRadar();
		main.init();
		
		boolean tu=true;
		while(tu){
			if(main.start){
				Thread.sleep(50);
				main.stepFromRadar();
			}else{
				Thread.sleep(50);
			}
		}
	}

	public void init() throws IOException, AWTException {

		radarReader = new RadarReader();
		if (ENABLE_RADAR) {
			radarReader.connect();
		}
		radarReader.setName("reader");
		if (ENABLE_RADAR) {
			radarReader.start();
		}
		loadSettings();
		read = new Read(file);
		data = new Data(divisor, accuracy, powerOfMaxGroup, powerOfVariance);
		data.setDepths(read.getDataByText());
		// data.generate(400, 5, 20);
		frame = new JFrame();
		view = new View(data,angle,scale,shift);
		view.mouse = mouse;
		view.minimumX = minimumX;
		view.minimumY = minimumY;
		view.maximumX = maximumX;
		view.maximumY = maximumY;

		Robot robot = new Robot();
		view.addMouseRobot((x, y) -> {
			robot.mouseMove(x, y);
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		});

		frame.add(view);
		JButton btn = new JButton("CLUSTER");
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				data.cluster();
				// data.display();
				frame.repaint();
			}

		});
		btn.setBounds(view.DEFAULT_SIZE, 0, view.WIDTH_BUTTON, 40);

		JButton btn2 = new JButton("UNCLUSTER");
		btn2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				data.unCluster();
				frame.repaint();
			}

		});
		btn2.setBounds(view.DEFAULT_SIZE, 40, view.WIDTH_BUTTON, 40);

		JButton btn3 = new JButton("START");
		btn3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (start) {
					start = false;
					btn3.setText("START");
				} else {
					start = true;
					btn3.setText("STOP");
				}

			}

		});
		btn3.setBounds(view.DEFAULT_SIZE, 80, view.WIDTH_BUTTON, 40);

		JButton btn4 = new JButton("DISCONNECT");
		btn4.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (start) {
					start = false;
					btn3.setText("START");
				} 
				radarReader.stop();
				radarReader.device.stopCapture();
				radarReader.device.disconnect();
			}

		});
		btn4.setBounds(view.DEFAULT_SIZE, 120, view.WIDTH_BUTTON, 40);
		
		view.setLayout(null);
		view.add(btn);
		view.add(btn2);
		view.add(btn3);
		view.add(btn4);

		/*sliderOfAccuracy = new JSlider(1, 1000, accuracy);
		sliderOfAccuracy.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				accuracy = slider.getValue();
				data.setAccuracy(accuracy);
				 try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		sliderOfAccuracy.setBounds(view.DEFAULT_SIZE, 190, view.WIDTH_BUTTON, 40);
		view.add(sliderOfAccuracy);*/
		checkboxMouse = new JCheckBox("Mouse", mouse);
		checkboxMouse.addActionListener(e -> {
			JCheckBox source = (JCheckBox) e.getSource();
			mouse = source.isSelected();
			view.mouse = mouse;
			try {
				saveSettings();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (data.isFlaged()) {
				data.unCluster();
				data.cluster();
			}
			frame.repaint();
		});
		checkboxMouse.setBounds(view.DEFAULT_SIZE, 310, view.WIDTH_BUTTON, 40);
		view.add(checkboxMouse);

		sliderOfMinimumX = new JSlider(0, View.DEFAULT_SIZE, minimumX);
		sliderOfMinimumX.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				minimumX = slider.getValue();
				view.minimumX=minimumX;
				 try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		sliderOfMinimumX.setBounds(view.DEFAULT_SIZE, 360, view.WIDTH_BUTTON, 40);
		view.add(sliderOfMinimumX);

		sliderOfMinimumY = new JSlider(0, View.DEFAULT_SIZE, minimumY);
		sliderOfMinimumY.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				minimumY = slider.getValue();
				view.minimumY=minimumY;
				 try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		sliderOfMinimumY.setBounds(view.DEFAULT_SIZE, 390, view.WIDTH_BUTTON, 40);
		view.add(sliderOfMinimumY);

		sliderOfMaximumX = new JSlider(0, View.DEFAULT_SIZE, maximumX);
		sliderOfMaximumX.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				maximumX = slider.getValue();
				view.maximumX=maximumX;
				 try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		sliderOfMaximumX.setBounds(view.DEFAULT_SIZE, 420, view.WIDTH_BUTTON, 40);
		view.add(sliderOfMaximumX);
		
		sliderOfMaximumY = new JSlider(0, View.DEFAULT_SIZE, maximumY);
		sliderOfMaximumY.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				maximumY = slider.getValue();
				view.maximumY=maximumY;
				 try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		sliderOfMaximumY.setBounds(view.DEFAULT_SIZE, 450, view.WIDTH_BUTTON, 40);
		view.add(sliderOfMaximumY);

		sliderOfShift = new JSlider(-100, 1000, shift);
		sliderOfShift.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				shift = slider.getValue();
				view.shift=shift;
				try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		sliderOfShift.setBounds(view.DEFAULT_SIZE, 480, view.WIDTH_BUTTON, 40);
		view.add(sliderOfShift);

		sliderOfDivisor = new JSlider(1, 500, divisor);
		sliderOfDivisor.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				divisor = slider.getValue();
				data.setDivisor(divisor);
				 try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		
		
		sliderOfDivisor.setBounds(view.DEFAULT_SIZE, 510, view.WIDTH_BUTTON, 40);
		view.add(sliderOfDivisor);

		sliderOfPowerOfMaxGroup = new JSlider(0, 100, powerOfMaxGroup);
		sliderOfPowerOfMaxGroup.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				powerOfMaxGroup = slider.getValue();
				powerOfVariance = 100 - powerOfMaxGroup;
				sliderOfPowerOfVariance.setValue(100 - powerOfMaxGroup);
				data.setPowerOfMaxGroup(powerOfMaxGroup);
				data.setPowerOfVariance(powerOfVariance);
				 try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		sliderOfPowerOfMaxGroup.setBounds(view.DEFAULT_SIZE, 540, view.WIDTH_BUTTON, 40);
		view.add(sliderOfPowerOfMaxGroup);

		sliderOfPowerOfVariance = new JSlider(0, 100, powerOfVariance);
		sliderOfPowerOfVariance.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				powerOfVariance = slider.getValue();
				powerOfMaxGroup = 100 - powerOfVariance;
				data.setPowerOfMaxGroup(powerOfMaxGroup);
				data.setPowerOfVariance(powerOfVariance);
				sliderOfPowerOfMaxGroup.setValue(100 - powerOfVariance);
				 try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		sliderOfPowerOfVariance.setBounds(view.DEFAULT_SIZE, 570, view.WIDTH_BUTTON, 40);
		view.add(sliderOfPowerOfVariance);

		sliderOfScale = new JSlider(1, 300, scale);
		sliderOfScale.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				scale=view.scale = slider.getValue();
				 try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		sliderOfScale.setBounds(view.DEFAULT_SIZE, 600, view.WIDTH_BUTTON, 40);
		view.add(sliderOfScale);

		sliderOfAngle = new JSlider(0, 360, angle);
		sliderOfAngle.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				angle=view.totalAngle = slider.getValue();
				 try {
					saveSettings();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (data.isFlaged()) {
					data.unCluster();
					data.cluster();
				}
				frame.repaint();
			}
		});
		sliderOfAngle.setBounds(view.DEFAULT_SIZE, 630, view.WIDTH_BUTTON, 40);
		view.add(sliderOfAngle);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setTitle("Cluster");

		frame.pack();

		frame.setVisible(true);
		
		//Thread.sleep(1);
				CaptureData tmp=radarReader.getDataFromRadar();
				//System.out.println(tmp.steps.get(0).distances.get(0));
				data.setDepthsFromRadar(tmp);
				
				if (data.isFlaged()) {
					data.cluster();
				}
				frame.repaint();
	}

	public void step() throws InterruptedException {
		ArrayList<Cell> tmp;
		Thread.sleep(200);
		tmp = read.getDataByText();
		if (tmp == null) {
			System.exit(0);
			;
		}
		data.setDepths(tmp);
		if (data.isFlaged()) {
			data.cluster();
		}
		frame.repaint();
	}
	

	public void stepFromRadar() throws InterruptedException {
		//Thread.sleep(1);
		CaptureData tmp=radarReader.getDataFromRadar();
		//System.out.println(tmp.steps.get(0).distances.get(0));
		data.setDepthsFromRadar(tmp);
		
		if (data.isFlaged()) {
			data.cluster();
		}
		frame.repaint();
	}
	
	
	public void loadSettings() throws FileNotFoundException, IOException{
		String tmpName="program.properties";
		if(new File(tmpName).exists()){
			Properties settings = new Properties();
			settings.load(new FileInputStream(tmpName));
			divisor= Integer.parseInt(settings.getProperty("divisor","10"));
			shift=Integer.parseInt(settings.getProperty("shift","100"));
			accuracy=Integer.parseInt(settings.getProperty("accuracy","100"));
			powerOfMaxGroup=Integer.parseInt(settings.getProperty("powerOfMaxGroup","0"));
			powerOfVariance=Integer.parseInt(settings.getProperty("powerOfVariance","100"));
			angle=Integer.parseInt(settings.getProperty("angle","360"));
			scale=Integer.parseInt(settings.getProperty("scale","120"));
			minimumX=Integer.parseInt(settings.getProperty("minimumX","0"));
			minimumY=Integer.parseInt(settings.getProperty("minimumY","0"));
			maximumX=Integer.parseInt(settings.getProperty("maximumX",""+View.DEFAULT_SIZE));
			maximumY=Integer.parseInt(settings.getProperty("maximumY","" + View.DEFAULT_SIZE));
			mouse=Boolean.parseBoolean(settings.getProperty("mouse","false"));
			file=settings.getProperty("file","1.txt");
		}
	}
	public void saveSettings() throws IOException{
		String tmpName="program.properties";
		Properties settings = new Properties();
		settings.put("divisor",  String.valueOf(divisor));
		settings.put("shift",  String.valueOf(shift));
		settings.put("accuracy",String.valueOf(accuracy));
		settings.put("powerOfMaxGroup",String.valueOf(powerOfMaxGroup));
		settings.put("powerOfVariance",String.valueOf(powerOfVariance));
		settings.put("angle",String.valueOf(angle));
		settings.put("scale",String.valueOf(scale));
		settings.put("minimumX",String.valueOf(minimumX));
		settings.put("minimumY",String.valueOf(minimumY));
		settings.put("maximumX",String.valueOf(maximumX));
		settings.put("maximumY",String.valueOf(maximumY));
		settings.put("mouse", ""+mouse);
		settings.put("file",file);
		FileOutputStream out = new FileOutputStream(tmpName);
		settings.store(out,"program properties");
	}
}
