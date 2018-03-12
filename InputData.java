import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.fazecast.jSerialComm.SerialPort;

public class SensorGraph {
	
	static SerialPort chosenPort;
	static int x = 0;
	static boolean EndThread;

	public static void main(String[] args) {
		
		// create and configure the window
		JFrame window = new JFrame();
		window.setTitle("Sensor Graph GUI");
		window.setSize(600, 400);
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// create a drop-down box and connect button, then place them at the top of the window
		JComboBox<String> portList = new JComboBox<String>();
		JButton connectButton = new JButton("Connect");
		JPanel topPanel = new JPanel();
		topPanel.add(portList);
		topPanel.add(connectButton);
		window.add(topPanel, BorderLayout.NORTH);
		
		// populate the drop-down box
		SerialPort[] portNames = SerialPort.getCommPorts();
		for(int i = 0; i < portNames.length; i++)
			portList.addItem(portNames[i].getSystemPortName());
		
		// create the line graph
		XYSeries series = new XYSeries("Resistance [ohms]");
		XYSeriesCollection dataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("Resistance [ohms]", "Time (seconds)", "ADC Reading", dataset);
		window.add(new ChartPanel(chart), BorderLayout.CENTER);
		
		// configure the connect button and use another thread to listen for data
		connectButton.addActionListener( new ActionListener(){
			
			@Override public void actionPerformed(ActionEvent arg0) {
				if (connectButton.getText().equals("Connect")) {
					connectButton.setText("Disconnect");
					//chosenPort.setBaudRate(9600);
						
						// create a new thread that listens for incoming text and populates the graph
						Thread thread = new Thread(){
							@Override public void run() {
								EndThread = false;
								// attempt to connect to the serial port
								chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
								chosenPort.openPort();
								chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
								chosenPort.setBaudRate(115200);
								java.io.InputStream MyInputStream = chosenPort.getInputStream();
								
								Scanner scanner = new Scanner(MyInputStream);
								while(EndThread == false){
										try {
											String line = scanner.nextLine();
											int number = Integer.parseInt(line);
											series.add(x++, 1023 - number);
											window.repaint();
										} catch(Exception e) {}
									try {
										Thread.currentThread().sleep(0, 500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									
									if (!chosenPort.isOpen()) {
										break;
									}
								}
								
								
								//Shutdown routine
								try {
									MyInputStream.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								scanner.close();
								portList.setEnabled(false);
								portList.setEnabled(true);
								chosenPort.closePort();
								connectButton.setText("Connect");
								EndThread = true;
								series.clear();
								x = 0;
								return;
						};
					};
					thread.start();
					
				} else {
					EndThread = true;
					}
			}//over ride
		});//action window
		
		// show the window
		window.setVisible(true);
	
	}
}
