# parallel
This program reads in a specific type of file which gives information used to calculate weather prediction. 
To use this program, simple put in your filename in the call for clouddata.readData() in the main program. The calculations will be done using parallization in clouddata.calculate(). After the calculations are done, the program will write the answer to the filename in the call for cloddata.writeData().

If it’s whished to run the code sequentially instead, simple uncomment calculateSequentialWind(); and calculateSequentialConvection(); and comment calculateParallelWind(); and calculateParallelConvection(); in the calculation method. 

The writeData() method then also needs to be change. Uncomment the part market as sequential printout and comment the part market as parallel printout .
