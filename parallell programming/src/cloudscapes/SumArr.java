package cloudscapes;

import java.util.concurrent.RecursiveTask;
//This class is used to calculate the wind with parallelization
public class SumArr extends RecursiveTask<Float>  {
	  int      lo;
	  int	   hi;
	  float[] arr;
	  static final int SEQUENTIAL_CUTOFF=100000;

	  int ans = 0; 
	    
    //constructor of class
	SumArr(float[] arr, int l, int h) { 
	    lo=l; 
	    hi=h;
	    this.arr=arr;
	  }

      //calculates wind using several threads, divides the work if size off array is < sequential cutoff
	  protected Float compute(){
		  if((hi-lo) < SEQUENTIAL_CUTOFF) {
			  float ans = 0;
		      for(int i=lo; i < hi; i++)
		        ans += arr[i];
		      return ans;
		  }
		  else {
			  SumArr left = new SumArr(arr,lo,(hi+lo)/2);
			  SumArr right= new SumArr(arr,(hi+lo)/2,hi);
			  
			  left.fork();
			  float rightAns = right.compute();
			  float leftAns  = left.join();
			  return leftAns + rightAns;     
		  }
	 }
}
