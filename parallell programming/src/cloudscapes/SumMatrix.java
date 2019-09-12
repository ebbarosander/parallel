package cloudscapes;

import java.util.concurrent.RecursiveTask;

/**
 * This class is used to calculate the convection with parallelization
 * @author 46709
 *
 */
public class SumMatrix extends RecursiveTask<int[]>  {
	
	static final int SEQUENTIAL_CUTOFF=100000;
	 float ans = 0; 
	 int l;
	 int h;
	 int dimx;
	 int dimy;
	 MyVector [][][] advection;
	 float [][][] convection;
	 int[] classification;
	 
	//constructor of class
	  SumMatrix(MyVector[][][] advection, float[][][] convection, int l, int h, int dimx, int dimy) { 
		  	this.advection=advection;
		  	this.convection=convection;
		    this.l=l; 
		    this.h=h; 
		    this.dimx=dimx;
		    this.dimy=dimy;
		    
		    classification = new int[h-l+1];
		  }
	  
	void locate(int pos, int [] ind)
	{
			ind[0] = (int) pos / (dimx*dimy); // t
			ind[1] = (pos % (dimx*dimy)) / dimy; // x
			ind[2] = pos % (dimy); // y
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

	 //calculates convection using several threads, divides the work if size off array is < sequential cutoff
	 protected int[] compute(){
		 int counter=0;
		 
		 
		 if((h-l) < SEQUENTIAL_CUTOFF) {
			 int[] arrlo= new int[3];
			 int[] arrhi= new int[3];
			
			   locate(l, arrlo);
			   locate(h, arrhi);
			
				for(int t = arrlo[0]; t <= arrhi[0]; t++) {
					for(int x = arrlo[1]; x <= arrhi[1]; x++) {
						for(int y = arrlo[2]; y <= arrhi[2]; y++){
							
							
							if(Math.abs(convection[t][x][y])>length(t,x,y)) { 
								classification[counter]=0;
							}else if(Math.abs(convection[t][x][y])<=length(t,x,y) && length(t,x,y)>0.2) { 
								classification[counter]=1;
							}else{
								classification[counter]=2;
							}	
							counter++;
				} }}
				
		      return classification;
		      
		  } else {
			  SumMatrix left = new SumMatrix(advection, convection, l,((h+l)/2), dimx, dimy);
			  SumMatrix right= new SumMatrix(advection, convection, ((h+l)/2)+1,h, dimx, dimy);
			  left.fork();
			  int[] rightAns = right.compute();
			  int[] leftAns  = left.join();
			  System.arraycopy(leftAns, 0, classification,0, leftAns.length);
			  System.arraycopy(rightAns, 0, classification, leftAns.length, rightAns.length);
			  return classification;     
		  }
	 }
}