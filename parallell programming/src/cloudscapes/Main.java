package cloudscapes;


public class Main {
	
	static CloudData clouddata = new CloudData();

	public static void main(String[] args) {
		
		clouddata.readData("simpledata.txt");
		for(int i=0; i<15; i++) {
		clouddata.calculate();
		}
		clouddata.writeData("largedataoutput.txt");
		
	
}

	
}
