package cloudscapes;

public class Main {
	
	static CloudData clouddata = new CloudData();

	public static void main(String[] args) {
		
		clouddata.readData("largedatainput.txt");		
		clouddata.calculate();	
		clouddata.writeData("largedataoutput.txt");
		
		
	}
				
	
}

	

