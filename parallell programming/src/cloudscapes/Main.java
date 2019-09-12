package cloudscapes;

/**
 * Main class 
 * Calls upon methods readdata, calculate and writedata 
 * from the CloudData class
 * @author Ebba Rosander
 *
 */
public class Main {
	
	static CloudData clouddata = new CloudData();

	public static void main(String[] args) {
		
		clouddata.readData(args[0]);		
		clouddata.calculate();	
		clouddata.writeData(args[1]);
		
		
	}
				
	
}

	

