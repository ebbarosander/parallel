package cloudscapes;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.ForkJoinPool;


public class CloudData {

	MyVector [][][] advection;// in-plane regular grid of wind vectors, that evolve over time
	MyVector wind= new MyVector(); 
	float [][][] convection; // vertical air movement strength, that evolves over time
	int [][][] classification; // cloud type per grid point, evolving over time
	int dimx, dimy, dimt; // data dimensions
	int dim=0;
	static long startTime=0;
	float time=0;
	static final ForkJoinPool fjPool = new ForkJoinPool();
	
	  float [] arrX;
	  float [] arrY;
	  int [] arrtot;
	  int position=0;
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
	
	// write classification output to file	
	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	private static float tock(){
		return (System.currentTimeMillis() - startTime) / 1000.0f; 
	}

	static Float sum(float[] arr){
	  return fjPool.invoke(new SumArr(arr,0,arr.length));
	}
	
	static int[] calculateConvection(MyVector[][][] advection, float [][][] convection,int l, int dim, int dimx, int dimy){
		  return fjPool.invoke(new SumMatrix(advection ,convection, 0, dim ,dimx, dimy));
		}


	public void calculate() {
		tick();
	
		
		for(int i=0; i<10; i++) {
		calculateSequentialWind();
		//calculateParallelWind();
		}
		time=tock();
		System.out.println("Calculation time for wind was " + time + " seconds" );	
		tick();
		for(int i=0; i<10; i++) {
		calculateSequentialConvection();
		
		//calculateParallelConvection();
		}
		
		time=tock();
		System.out.println("Calculation time for convection was " + time/10 + " seconds" );
		
				}
		

	private int higher(int position, int dimboundry) {
		if(position+1==dimboundry) {
			return position;
		}else {
			return (position+1);			
		}
	}

	private int lower(int position) {		
		if (position==0) {
			return 0;
		}else {
			return (position-1);
		}
	}
	
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
	
	private void calculateParallelWind() {
		wind.x=sum(arrX)/dim();
		wind.y=sum(arrY)/dim();		
	}

	private void calculateParallelConvection() {
		arrtot=calculateConvection(advection, convection,0,dim-1, dimx, dimy);
	}

	void writeData(String fileName){
		
		 try{ 
			 FileWriter fileWriter = new FileWriter(fileName);
			 PrintWriter printWriter = new PrintWriter(fileWriter);
			 printWriter.printf("%d %d %d\n", dimt, dimx, dimy);
			 printWriter.printf("%f %f\n", wind.x, wind.y);
			 
			 //Sequential printout
			 //for(int t = 0; t < dimt; t++){
				// for(int x = 0; x < dimx; x++){
					//for(int y = 0; y < dimy; y++){
						//printWriter.printf("%d ", classification[t][x][y]);
					//}
				 //}
			 //}

			 
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

		
	
