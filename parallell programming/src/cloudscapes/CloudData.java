package cloudscapes;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.ForkJoinPool;

/**
 * Class responsible for
 * reading in input file
 * contains methods that calculate sequential
 * contains calls to class SumMatrix and SumArr
 * write out to output file
 * @author Ebba Rosander
 *
 */
public class CloudData {

	MyVector [][][] advection;// in-plane regular grid of wind vectors, that evolve over time
	MyVector wind= new MyVector(); // class that contains two floats, x and y
	float [][][] convection; // vertical air movement strength, that evolves over time
	int [][][] classification; // cloud type per grid point, evolving over time
	int dimx, dimy, dimt; // data dimensions
	int dim=0; //=dim();
	static long startTime=0; //used to store start time of timing
	float time=0; //used to store stop time- start time/ time taken to perform measured task
	static final ForkJoinPool fjPool = new ForkJoinPool();
	
	  float [] arrX;//filled with all x value after readdata method is called upon
	  float [] arrY;//filled with all y value after readdata method is called upon
	  int [] arrtot;//filled with all values after readdata method is called upon
	  int position=0; //Keeps track on were in the array the elements should be added to
	  
	// overall number of elements in the timeline grids
	int dim(){
		return dimt*dimx*dimy;
	}
	
	
	// read cloud simulation data from file
	void readData(String fileName){ 
		try{ 
			Scanner sc = new Scanner(new File(fileName), "UTF-8");
			
			sc.useLocale(Locale.US);
			
			dimt = sc.nextInt();
			dimx = sc.nextInt(); 
			dimy = sc.nextInt();
			
			dim= dim();
			
			arrX= new float [dim];
			arrY= new float [dim];
			arrtot= new int [dim];
			
			
			
			// initialize and load advection (wind direction and strength) and convection
			advection = new MyVector[dimt][dimx][dimy];
			convection = new float[dimt][dimx][dimy];
			for(int t = 0; t < dimt; t++)
				for(int x = 0; x < dimx; x++)
					for(int y = 0; y < dimy; y++){
						advection[t][x][y] = new MyVector();
						
						advection[t][x][y].x = sc.nextFloat();
						arrX[position]= advection[t][x][y].x;
						
					
						advection[t][x][y].y = sc.nextFloat();
						arrY[position]= advection[t][x][y].y;
						
						
						convection[t][x][y] = sc.nextFloat();
						
						
						position++;
					}
			
			classification = new int[dimt][dimx][dimy];
			sc.close(); 
		} 
		catch (IOException e){ 
			System.out.println("Unable to open input file "+fileName);
			e.printStackTrace();
		}
		catch (java.util.InputMismatchException e){ 
			System.out.println("Malformed input file "+fileName);
			e.printStackTrace();
		}
	}
	
	// saves start time
	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	//returns stop time-start time
	private static float tock(){
		return (System.currentTimeMillis() - startTime) / 1000.0f; 
	}
    //starts compute in SumArr class
	static Float sum(float[] arr){
	  return fjPool.invoke(new SumArr(arr,0,arr.length));
	}
	//starts compute in SumMatrix class
	static int[] calculateConvection(MyVector[][][] advection, float [][][] convection,int l, int dim, int dimx, int dimy){
		  return fjPool.invoke(new SumMatrix(advection ,convection, 0, dim ,dimx, dimy));
		}

    //Calculates wind and convection. Is either calling on sequential algorithm or parallel algorithm
	public void calculate() {
		
		tick();
	    //calculateSequentialWind();
		calculateParallelWind();	
		time=tock();
		
		
		tick();
		//calculateSequentialConvection();
		calculateParallelConvection();	
		time=tock();
		
				}
		
	//returns index of neighbor right or below if existing
	private int higher(int position, int dimboundry) {
		if(position+1==dimboundry) {
			return position;
		}else {
			return (position+1);			
		}
	}
	//returns index of neighbor left or above if existing
	private int lower(int position) {		
		if (position==0) {
			return 0;
		}else {
			return (position-1);
		}
	}
	
	//returns the length of a vector, where the vector is represented by one positions x and y values + its neighbors 
	private float length(int t,int x, int y) {
		float w=0;
		float xvalue=0;
		float yvalue=0;
		int numberofpositions=0;
			
	for(int j=lower(y); j<=higher(y,dimy); j++) 
		for(int i=lower(x); i<=higher(x,dimx); i++) {
			xvalue +=advection[t][i][j].x;
			yvalue +=advection[t][i][j].y;
			numberofpositions++; 
			}
		
		w= (float) Math.sqrt(Math.pow(xvalue/numberofpositions, 2) + Math.pow(yvalue/numberofpositions,2));
		return w;
	}
    //calculates convection sequentially 
	private void calculateSequentialConvection() {
					
					
		for(int t = 0; t < dimt; t++)
			for(int x = 0; x < dimx; x++)
				for(int y = 0; y < dimy; y++){
					if(Math.abs(convection[t][x][y])>length(t,x,y)) { 
						classification[t][x][y]=0;
					}else if(Math.abs(convection[t][x][y])<=length(t,x,y) && length(t,x,y)>0.2) { 
						classification[t][x][y]=1;
					}else{
						classification[t][x][y]=2;
					}	
		} 
		
	}
	//calculates wind sequentially
	private void calculateSequentialWind() {
		float xtotal =0;
		float ytotal =0;
		
		for(int t = 0; t < dimt; t++)
			for(int x = 0; x < dimx; x++)
				for(int y = 0; y < dimy; y++){
					xtotal+= advection[t][x][y].x;
					ytotal+= advection[t][x][y].y;
				}	
	wind.x=xtotal/dim();
	wind.y=ytotal/dim();
		
	}
	//calculates wind parallel
	private void calculateParallelWind() {
		wind.x=sum(arrX)/dim();
		wind.y=sum(arrY)/dim();		
	}
	//calculates convection parallel
	private void calculateParallelConvection() {
		arrtot=calculateConvection(advection, convection,0,dim-1, dimx, dimy);
	}
	//writes result to file filename
	void writeData(String fileName){
		
		 try{ 
			 FileWriter fileWriter = new FileWriter(fileName);
			 PrintWriter printWriter = new PrintWriter(fileWriter);
			 printWriter.printf("%d %d %d\n", dimt, dimx, dimy);
			 printWriter.printf(Locale.US,"%f %f\n", wind.x, wind.y);
			 
			 //Sequential printout
//			 for(int t = 0; t < dimt; t++){
//				 for(int x = 0; x < dimx; x++){
//					for(int y = 0; y < dimy; y++){
//						printWriter.printf("%d ", classification[t][x][y]);
//					}
//				 }
//			 }
			 

			 
			 //Parallel printout
					for(int y = 0; y < dim; y++){
						printWriter.printf("%d ", arrtot[y]);
						if(y% dimx*dimy == dim()) {
							printWriter.printf("\n");
						}
					}
		 
				 
				 printWriter.printf("\n");
			 
	
				 
			 printWriter.close();
	}catch (IOException e){
			 System.out.println("Unable to open output file "+fileName);
				e.printStackTrace();
		 }
	}
}




		
	
