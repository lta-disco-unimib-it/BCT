/**
* Copyright 2005 Victor Ferrer
* 
* This file is part of FreeSudoku.
* 
* FreeSudoku is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
* 
* FreeSudoku is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with FreeSudoku; if not, write to the Free Software
* Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
*/
package org.gos.freesudoku;

import java.util.*;

/**
 * @author gos
 *
 */
public class Solver
{

	private int[][] 	matriu 			= null;
	private int 		filled 			= 0;
	protected ArrayList solutions       = null;
    protected String    strategiesUsed  = null;
    protected int       difficulty      = 0;
    private static Hashtable cache    = null;

    /**
     * Comment for <code>posVals</code>
     * For each i,j position in board
	 * contains an array of posible values:
	 * posVals[i][j][0] -> number of posible values
	 * posvals[i][j][n] -> 1 if n is a posible value
	 *		where 1 <= n <= 9
     */
    private int[][][]	posVals			= null;
    
    /**
     * Comment for <code>groups</code>
     * There are 27 groups of 9 cells; 9 rows + 9 columns + 9 3x3_blocks 
     * For each group we can apply the different strategies.
     * Each position is just a pointer to a posVals element. 
     */
    private int[][][]   groups          = null;
    
	

	public Solver()
	{
		matriu            = new int[9][9];
		solutions         = new ArrayList();
		posVals       	  = new int[9][9][10];
        if( cache == null) cache = new Hashtable();
	}
    
    protected void setMatriu(int[][] pMatriu)
    {
        strategiesUsed    = "";
        difficulty = 0;
    	filled = 0;
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                matriu[i][j] = pMatriu[i][j];
                if( matriu[i][j] != 0) filled++;
            }
        }
        fillPosVals();
        setGroups();
//        CONSTS.log( " *** Matriu initializated:\n" + matriuToString( matriu));
    }
	
    private void setGroups()
    {
        groups = new int[27][9][];
        int cont = 0;

        for (int n = 0; n < 9; n++)
        {
            // add rows
            for (int m = 0; m < 9; m++)
            {
                groups[cont][m] = posVals[n][m];
            }
            cont++;

            // add cols
            for (int m = 0; m < 9; m++)
            {
                groups[cont][m] = posVals[m][n];
            }
            cont++;
        }
        
        // add boxes
        for (int i = 0; i < 9; i+=3)
        {
            for (int j = 0; j < 9; j+=3)
            {
                groups[cont][0] = posVals[i][j];               
                groups[cont][1] = posVals[i][j+1];             
                groups[cont][2] = posVals[i][j+2];             
                groups[cont][3] = posVals[i+1][j];             
                groups[cont][4] = posVals[i+1][j+1];               
                groups[cont][5] = posVals[i+1][j+2];               
                groups[cont][6] = posVals[i+2][j];             
                groups[cont][7] = posVals[i+2][j+1];               
                groups[cont][8] = posVals[i+2][j+2];
                cont++;
            }
        }
        
    }
    
    protected int getAllSols()
    {
        int sols = 0;
        if( filled == 81) 
        { 
            CONSTS.log( "************* SOLUCIO ********************\n" + matriuToString( matriu));
        	solutions.add( dupArray( matriu));
            return 1;
        }
        CONSTS.log(" ++++  Filled: " + filled);
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                if( matriu[i][j] == 0)
                {
                    ArrayList arrayPos = Game.getCorrectValsForArray( i, j, matriu);
                    if( arrayPos.isEmpty()) 
                    {
                        CONSTS.log(" ------ 0   No posible number! " + i + " " + j);
                        return 0;
                    }
                }
            }
        }

        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                if( matriu[i][j] == 0)
                {
                    CONSTS.log( matriuToString( matriu));
                    ArrayList arrayPos = Game.getCorrectValsForArray( i, j, matriu);

                    filled++;
                    for (int n = 0; n < arrayPos.size(); n++)
                    {
                        matriu[i][j] = ((Integer )arrayPos.get( n)).intValue();
//                        CONSTS.log("Checking " + i + " " + j + " " + matriu[i][j] + " Filled: " + filled);
                        sols += getAllSols();
                    }
                    matriu[i][j] = 0;
                    filled--;
//                    CONSTS.log(" ----- Checked " + i + " " + j + " sols: " + sols);
                    return sols;
                }
            }
        }
//        CONSTS.log(" ------ End check matriu: sols" + sols + "\n" + matriuToString( matriu));
        return sols;
    }

    
    protected static int[][] dupArray(int[][] pArray)
	{
    	int[][] res = new int[9][9];
    	for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
            	res[i][j] = pArray[i][j];
            }
        }
		return res;
	}

	/**
     * @return 0: No solution, 1:only one solution, 2:more than one
     */
    protected int uniqueSol()
    {
        int sols = 0;
        if( filled == 81) 
        { 
//            CONSTS.log( "************* SOLUCIO ********************\n" + matriuToString( matriu));
            solutions.add( dupArray( matriu));
            return 1;
        }
//        CONSTS.log("uniqueSol ++++  Filled: " + filled);
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                if( matriu[i][j] == 0)
                {
                    ArrayList arrayPos = Game.getCorrectValsForArray( i, j, matriu);
                    if( arrayPos.isEmpty()) 
                    {
  //                      CONSTS.log("uniqueSol ------ 0   No posible number! " + i + " " + j);
                        return 0;
                    }
                }
            }
        }

        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                if( matriu[i][j] == 0)
                {
//                    CONSTS.log("Checking " + i + " " + j + " Filled: " + filled);
//                    CONSTS.log( matriuToString( matriu));
                    ArrayList arrayPos = Game.getCorrectValsForArray( i, j, matriu);

                    filled++;
                    for (int n = 0; n < arrayPos.size(); n++)
                    {
                        matriu[i][j] = ((Integer )arrayPos.get( n)).intValue();
                        sols += uniqueSol();
                        if( sols > 1) 
                        {
//                            CONSTS.log("nivell " + filled + " sols " + sols);
//                            CONSTS.log("i j n " + i + " " + j + " " + n);
                            break;
                        }
                    }
                    matriu[i][j] = 0;
                    filled--;
//                    CONSTS.log(" ----- Checked " + i + " " + j + " sols: " + sols);
                    return (sols > 1 ? 2 : sols);
                }
            }
        }
//        CONSTS.log(" ------ End check matriu: sols" + sols + "\n" + matriuToString( matriu));
        return sols;
    }

    
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int[][] aux = 
		{

//				 2 9 7 4 1 8 6 3 5
//				 5 8 1 9 3 6 7 4 2
//				 6 4 3 2 5 7 8 1 9
//				 9 5 8 7 2 3 1 6 4
//				 3 1 4 5 6 9 2 7 8
//				 7 2 6 1 8 4 5 9 3
//				 4 7 5 6 9 2 3 8 1
//				 1 3 9 8 7 5 4 2 6
//				 8 6 2 3 4 1 9 5 7

                {0,0,0,0,0,0,1,0,7},
                {0,7,0,0,0,6,0,3,0},
                {2,0,0,7,0,0,5,0,0},
                {0,0,4,1,2,0,6,5,0},
                {3,0,0,0,0,0,0,0,1},
                {0,5,7,0,3,8,4,0,0},
                {0,0,1,0,0,3,0,0,5},
                {0,6,0,8,0,0,0,4,0},
                {5,0,9,0,0,0,0,0,0}
        };
        
//        matriuFromKey(  "010900000" +
//                        "820706010" +
//                        "075000200" +
//                        "060800000" +
//                        "109000507" +
//                        "000001040" +
//                        "006000870" +
//                        "040508029" +
//                        "000007060");
				
		
		Solver solver = new Solver();
        solver.setMatriu( aux);
        solver.resetCache();
        CONSTS.log( solver.posibleValsToString());
        boolean changed = false;
        solver.loopAllStg();
        CONSTS.log( solver.toString());
        CONSTS.log( "Filled: " + solver.filled);
        CONSTS.log( solver.strategiesUsed);
        CONSTS.log( "Diff: " + solver.difficulty);
	}

    public static String matriuToString( int[][] pArray)
    {
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                res.append( " " + pArray[i][j]);
            }
            res.append("\n");
        }
        return res.toString();
    }
    
    protected static String matriuToKey( int[][] pArray)
    {
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                res.append( pArray[i][j]);
            }
        }
        return res.toString();
    }
    
    protected static int[][] matriuFromKey( String key)
    {
        int[][] res = new int[9][9]; 
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                res[i][j] = Integer.parseInt( String.valueOf( key.charAt( i*9 + j)));
            }
        }
        return res;
    }
    
	private boolean applyStrategy_2()
    {
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                if (matriu[i][j] != 0) continue;
                if (posVals[i][j][0] == 1)
                {
                	for (int k = 1; k <= 9; k++)
					{
						if( posVals[i][j][k] == 1) 
						{
							setVal( i, j, k);
                            logStrategy(2);
                            return true;
						}
					}
                }
            }
        }
//        CONSTS.log( this.toString());
        return false;
    }
    private void setVal(int i, int j, int val)
	{
    	matriu[i][j] = val;
  	  	filled++;
//    	CONSTS.log("setVal " + i + " " + j + " -> " + val + " filled: " + filled);
  	  	checkPosibleValsFor( i, j, val);
	}

	private void checkPosibleValsFor(int i, int j, int val)
	{
    	Arrays.fill( posVals[i][j], 0);	// remove all posible vals
		posVals[i][j][0] = -val;  		
		removeNumFromRow( val, i, -1);  // remove from all 3 boxes
		removeNumFromCol( val, j, -1);  // remove from all 3 boxes
		removeNumFromBox( val, i, j, -1, -1);
	}

	private boolean applyStrategy_1()
	{
		// TODO: recode to check per blocks and call subrutine...
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				if (matriu[i][j] != 0) continue;
				for (int n = 1; n <= 9; n++)
				{
					if(posVals[i][j][n] != 1) continue;
					boolean found;

					// check block
					found = false;
					for (int x = i - i%3; x < i - i%3 + 3; x++)
					{
						for (int y = j - j%3; y < j - j%3 + 3; y++)
						{
							if ((x == i && y == j) || matriu[x][y] != 0)
								continue;
							if ( posVals[x][y][n] == 1)
							{
								found = true;
								break;
							} 
						}
						if( found) break;
					}
					if (!found)
					{
						setVal( i, j, n);
                        logStrategy(1);
						return true;
					}

					// check row
					found = false;
					for (int x = 0; x < 9; x++)
					{
						if (x == j || matriu[i][x] != 0)
							continue;
						if ( posVals[i][x][n] == 1 )
						{
							found = true;
							break;
						}
					}
					if (!found)
					{
						setVal( i, j, n);
                        logStrategy(1);
                        return true;
					}

					// check column
					found = false;
					for (int x = 0; x < 9; x++)
					{
						if (x == i || matriu[x][j] != 0)
							continue;
						if ( posVals[x][j][n] == 1)
						{
							found = true;
							break;
						}
					}
					if (!found)
					{
						setVal( i, j, n);
                        logStrategy(1);
                        return true;
					}

				} // for n
			} // for j
		} // for i
		return false;
	}
    
    private boolean applyStrategy_3_locked_candidates_1()
    {
    	// Some masive copy+paste code, but better than offuscate too much
    	// Not more computer efficient, but more similar to lazzy human behaviour
    	// Goal is to apply minimum times this test, not to the whole board.
    	// After one single hit and change, we exit to start with the easier tests again.
    	
//    	CONSTS.log("-----------------------------   Start Strategy_3_locked_candidates_1");
    	boolean posValsChanged = false;
    	for (int num = 1; num <= 9; num++)
		{

    		///////////// check lines ///////////////////////
    		for (int line = 0; line < 9; line+=3)
			{
			 for (int col = 0; col < 9; col+=3)
			 {
				if(	(	posVals[line][col+0][num] == 1 ||	// num is in one row of box
						posVals[line][col+1][num] == 1 ||
						posVals[line][col+2][num] == 1)
					&&
					(	posVals[line+1][col+0][num] != 1 && // and not in the othe rows
						posVals[line+1][col+1][num] != 1 &&
						posVals[line+1][col+2][num] != 1 &&
						posVals[line+2][col+0][num] != 1 &&
						posVals[line+2][col+1][num] != 1 &&
						posVals[line+2][col+2][num] != 1))
				{
//					CONSTS.log("removeNumFromRow line " + line + " col " + col + " num " + num);
					posValsChanged = removeNumFromRow( num, line, col);
					if( posValsChanged)
                    {
                        logStrategy( 3);
                        return true;
                    }
													// is not the most efficient way, 
													// but human behaviour:
													// after finding a new number, start over
													// with the easier test, not continue here.
													// Important to rate difficult level
				} else 
				if((	posVals[line+1][col+0][num] == 1 ||	// num is in one row of box
						posVals[line+1][col+1][num] == 1 ||
						posVals[line+1][col+2][num] == 1)
					&&
					(	posVals[line][col+0][num]   != 1 && // and not in the othe rows
						posVals[line][col+1][num]   != 1 &&
						posVals[line][col+2][num]   != 1 &&
						posVals[line+2][col+0][num] != 1 &&
						posVals[line+2][col+1][num] != 1 &&
						posVals[line+2][col+2][num] != 1))
				{
					// CONSTS.log("removeNumFromRow line " + line + " col " + col + " num " + num);
					posValsChanged = removeNumFromRow( num, line+1, col);
                    if( posValsChanged)
                    {
                        logStrategy(3);
                        return true;
                    }
				} else 
					if((	posVals[line+2][col+0][num] == 1 ||	// num is in one row of box
							posVals[line+2][col+1][num] == 1 ||
							posVals[line+2][col+2][num] == 1)
						&&
						(	posVals[line][col+0][num]   != 1 && // and not in the othe rows
							posVals[line][col+1][num]   != 1 &&
							posVals[line][col+2][num]   != 1 &&
							posVals[line+1][col+0][num] != 1 &&
							posVals[line+1][col+1][num] != 1 &&
							posVals[line+1][col+2][num] != 1))
					{
						// CONSTS.log("removeNumFromRow line " + line + " col " + col + " num " + num);
						posValsChanged = removeNumFromRow( num, line+2, col);
                        if( posValsChanged)
                        {
                            logStrategy(3);
                            return true;
                        }
					}
			 } // for col
			} // for line
    		///////////// End check lines ///////////////////////

	    		///////////// check cols ///////////////////////
				for (int col = 0; col < 9; col+=3)
	    		{
				 for (int line = 0; line < 9; line+=3)
				 {
					if(	(	posVals[line+0][col][num] == 1 ||	// num is in one row of box
							posVals[line+1][col][num] == 1 ||
							posVals[line+2][col][num] == 1)
						&&
						(	posVals[line+0][col+1][num] != 1 && // and not in the othe rows
							posVals[line+1][col+1][num] != 1 &&
							posVals[line+2][col+1][num] != 1 &&
							posVals[line+0][col+2][num] != 1 &&
							posVals[line+1][col+2][num] != 1 &&
							posVals[line+2][col+2][num] != 1))
					{
						// CONSTS.log("removeNumFromCol line " + line + " col " + col + " num " + num);
						posValsChanged = removeNumFromCol( num, col, line);
                        if( posValsChanged)
                        {
                            logStrategy(3);
                            return true;
                        }
					} else 
						if(	(	posVals[line+0][col+1][num] == 1 ||	// num is in one row of box
								posVals[line+1][col+1][num] == 1 ||
								posVals[line+2][col+1][num] == 1)
							&&
							(	posVals[line+0][col+0][num] != 1 && // and not in the othe rows
								posVals[line+1][col+0][num] != 1 &&
								posVals[line+2][col+0][num] != 1 &&
								posVals[line+0][col+2][num] != 1 &&
								posVals[line+1][col+2][num] != 1 &&
								posVals[line+2][col+2][num] != 1))
						{
							// CONSTS.log("removeNumFromCol line " + line + " col " + col + " num " + num);
							posValsChanged = removeNumFromCol( num, col+1, line);
                            if( posValsChanged)
                            {
                                logStrategy(3);
                                return true;
                            }
						} else
							if(	(	posVals[line+0][col+2][num] == 1 ||	// num is in one row of box
									posVals[line+1][col+2][num] == 1 ||
									posVals[line+2][col+2][num] == 1)
								&&
								(	posVals[line+0][col+1][num] != 1 && // and not in the othe rows
									posVals[line+1][col+1][num] != 1 &&
									posVals[line+2][col+1][num] != 1 &&
									posVals[line+0][col+0][num] != 1 &&
									posVals[line+1][col+0][num] != 1 &&
									posVals[line+2][col+0][num] != 1))
							{
								// CONSTS.log("removeNumFromCol line " + line + " col " + col + " num " + num);
								posValsChanged = removeNumFromCol( num, col+2, line);
                                if( posValsChanged)
                                {
                                    logStrategy(3);
                                    return true;
                                }
							} 
				 } // for line
				} // for col
		    	///////////// End check cols ///////////////////////
				
		} // for num
    	
        return false;
    }

    /**
     * @param i
     */
    private void logStrategy(int diff)
    {
        strategiesUsed += diff;
        if( diff > 2) difficulty += diff*4;
        else if( diff == 2)difficulty += 1;
        // CONSTS.log("*** Strategy " + diff + " appl. Difficulty: " + difficulty + " filled " + filled + " - "+ strategiesUsed );
    }

    private boolean applyStrategy_4_locked_candidates_2()
    {
    	
//    	CONSTS.log("-----------------------------   Start Strategy_4_locked_candidates_2");
    	boolean posValsChanged = false;
    	for (int num = 1; num <= 9; num++)
		{

    		///////////// check rows ///////////////////////
    		for (int line = 0; line < 9; line++)
			{
    			if(	(	posVals[line][0][num] == 1 ||
    					posVals[line][1][num] == 1 ||
    					posVals[line][2][num] == 1)
    				&&
    				(	posVals[line][3][num] != 1 &&
    					posVals[line][4][num] != 1 &&
    					posVals[line][5][num] != 1 &&
    					posVals[line][6][num] != 1 &&
    					posVals[line][7][num] != 1 &&
    					posVals[line][8][num] != 1 )
    			)
    			{
    				// CONSTS.log("removeNumFromBox line " + line + " box 1" + " num " + num);
                    posValsChanged = removeNumFromBox( num, line, 0, line, -1);
                    if( posValsChanged)
                    {
                        logStrategy(4);
                        return true;
                    }
    			} else
        			if(	(	posVals[line][3][num] == 1||
        					posVals[line][4][num] == 1||
        					posVals[line][5][num] == 1)
        				&&
        				(	posVals[line][0][num] != 1 &&
        					posVals[line][1][num] != 1 &&
        					posVals[line][2][num] != 1 &&
        					posVals[line][6][num] != 1 &&
        					posVals[line][7][num] != 1 &&
        					posVals[line][8][num] != 1 )
        			)
        			{
        				// CONSTS.log("removeNumFromBox line " + line + " box 2" + " num " + num);
                        posValsChanged = removeNumFromBox( num, line, 3, line, -1);
                        if( posValsChanged)
                        {
                            logStrategy(4);
                            return true;
                        }
        			} else
            			if(	(	posVals[line][6][num] == 1||
            					posVals[line][7][num] == 1||
            					posVals[line][8][num] == 1)
            				&&
            				(	posVals[line][0][num] != 1 &&
            					posVals[line][1][num] != 1 &&
            					posVals[line][2][num] != 1 &&
            					posVals[line][3][num] != 1 &&
            					posVals[line][4][num] != 1 &&
            					posVals[line][5][num] != 1 )
            			)
            			{
            				// CONSTS.log("removeNumFromBox line " + line + " box 3" + " num " + num);
                            posValsChanged = removeNumFromBox( num, line, 6, line, -1);
                            if( posValsChanged)
                            {
                                logStrategy(4);
                                return true;
                            }
            			}
    			
			}
    		///////////// end check rows ///////////////////////

    		///////////// check cols ///////////////////////
    		for (int col = 0; col < 9; col++)
			{
    			if(	(	posVals[0][col][num] == 1||
    					posVals[1][col][num] == 1||
    					posVals[2][col][num] == 1)
    				&&
    				(	posVals[3][col][num] != 1 &&
    					posVals[4][col][num] != 1 &&
    					posVals[5][col][num] != 1 &&
    					posVals[6][col][num] != 1 &&
    					posVals[7][col][num] != 1 &&
    					posVals[8][col][num] != 1 )
    			)
    			{
    				// CONSTS.log("removeNumFromBox col " + col + " box 1" + " num " + num);
                    posValsChanged = removeNumFromBox( num, 0, col, -1, col);
                    if( posValsChanged)
                    {
                        logStrategy(4);
                        return true;
                    }
    			} else
        			if(	(	posVals[3][col][num] == 1||
        					posVals[4][col][num] == 1||
        					posVals[5][col][num] == 1)
        				&&
        				(	posVals[0][col][num] != 1 &&
        					posVals[1][col][num] != 1 &&
        					posVals[2][col][num] != 1 &&
        					posVals[6][col][num] != 1 &&
        					posVals[7][col][num] != 1 &&
        					posVals[8][col][num] != 1 )
        			)
        			{
        				// CONSTS.log("removeNumFromBox col " + col + " box 2" + " num " + num);
                        posValsChanged = removeNumFromBox( num, 3, col, -1, col);
                        if( posValsChanged)
                        {
                            logStrategy(4);
                            return true;
                        }
        			} else
            			if(	(	posVals[6][col][num] == 1||
            					posVals[7][col][num] == 1||
            					posVals[8][col][num] == 1)
            				&&
            				(	posVals[0][col][num] != 1 &&
            					posVals[1][col][num] != 1 &&
            					posVals[2][col][num] != 1 &&
            					posVals[3][col][num] != 1 &&
            					posVals[4][col][num] != 1 &&
            					posVals[5][col][num] != 1 )
            			)
            			{
            				// CONSTS.log("removeNumFromBox col " + col + " box 3" + " num " + num);
                            posValsChanged = removeNumFromBox( num, 6, col, -1, col);
                            if( posValsChanged)
                            {
                                logStrategy(4);
                                return true;
                            }
            			}
    			
			}
    		///////////// end check cols ///////////////////////

		}
        return false;
    }

    private boolean applyStrategy_5_naked_pairs()
    {
    	
//    	CONSTS.log("-----------------------------   Start applyStrategy_5_naked_pairs");
//    	CONSTS.log( toString());
//    	CONSTS.log( posibleValsToString());
    	boolean posibleValsChanged = false;
    	
        for (int n = 0; n < 27; n++)
        {
            posibleValsChanged = checkGroupForNakedPairs( groups[n]);
            if( posibleValsChanged )
            {
                logStrategy(5);
                return true;
            }
        }
        return false;
    }

    private boolean applyStrategy_6_hidden_pairs()
    {
    	// *** warning!
    	// TODO: this method is almost identical than applyStrategy_5_naked_pairs  -> merge
    	//
//    	CONSTS.log("-----------------------------   Start applyStrategy_6_hidden_pairs");
//    	CONSTS.log( toString());
//    	CONSTS.log( posibleValsToString());
    	boolean posibleValsChanged = false;

        for (int n = 0; n < 27; n++)
        {
            posibleValsChanged = checkGroupForHiddenPairs( groups[n]);
            if( posibleValsChanged )
            {
                logStrategy(6);
                return true;
            }
        }
        return false;
    }

    private boolean checkGroupForHiddenPairs(int[][] group)
	{
		boolean posibleValsChanged = false;
		int[][] pairs = getAllPairsFromGroup( group);
		
		for (int p = 0; p < pairs.length; p++)
		{
			int found1, found2 = -1;
			for (int n = 0; n < group.length; n++)
			{
				if( group[n][0] < 2) continue;
				if( group[n][ pairs[p][0]] == 1 && group[n][ pairs[p][1]] == 1 )
				{
					found1 = n;
					for (int m = n+1; m < group.length; m++)
					{
						if( group[m][0] < 2) continue;
						if( group[m][ pairs[p][0]] == 1 && group[m][ pairs[p][1]] == 1 )
						{
							found2 = m;
							// check if exists one of the vals in another cell
							for (int x = 0; x < 9; x++)
							{
								if( x == found1 || x == found2) continue;
								if( group[x][ pairs[p][0]] == 1 || group[x][ pairs[p][1]] == 1 )
								{
									found2 = -1;
									break;
								}
							}
							if( found2 != -1)  // we found a real hidden pair -> check if we can remove a posible val.
							{
//								CONSTS.log("HIDDEN PAIR found: " + pairs[p][0] + " " + pairs[p][1]);
								if( group[found1][0] > 2 || group[found2][0] > 2)
								{
                                    Arrays.fill( group[found1], 0); // clear array
                                    group[found1][ pairs[p][0]] = 1;
                                    group[found1][ pairs[p][1]] = 1;
                                    group[found1][0] = 2;
                                    Arrays.fill( group[found2], 0); // clear array
                                    group[found2][ pairs[p][0]] = 1;
                                    group[found2][ pairs[p][1]] = 1;
                                    group[found2][0] = 2;
									posibleValsChanged = true;
									//CONSTS.log("HIDDEN PAIR. Pos val removed!");
									break;
								}
							}
						}
					} // for m
				}
				if( posibleValsChanged) break;
			} // for n
			if( posibleValsChanged) break;
		} // for p
    	
    	return posibleValsChanged;
	}

	private int[][] getAllPairsFromGroup(int[][] group)
	{
		int[][] res;

		ArrayList aux = new ArrayList();
		int[] par;
		boolean repeated;
		for (int n = 0; n < 9; n++)
		{
			for (int pos1 = 1; pos1 <= 9; pos1++)
			{
                if( group[n][pos1] == 0) continue;
				for (int pos2 = pos1+1; pos2 <= 9; pos2++)
				{
                    if( group[n][pos2] == 0) continue;
                    
					par = new int[2];
					par[0] = pos1;
					par[1] = pos2;
					repeated = false;
					for (int i = 0; i < aux.size(); i++)
					{
						if( 	par[0] == ((int[] )aux.get(i))[0] &&
								par[1] == ((int[] )aux.get(i))[1])
						{
							repeated = true;  // already in list
							break;
						}
					}
					if( !repeated)
					{
						aux.add( par);
					}
				}
				
			}
		}

		res = new int[aux.size()][2];
		for (int i = 0; i < aux.size(); i++)
		{
            res[i] = (int[] )aux.get(i);
		}
		return res;
	}

    private boolean applyStrategy_7_naked_triples()
    {
//        CONSTS.log("-----------------------------   Start applyStrategy_7_naked_triples");
//        CONSTS.log( toString());
//        CONSTS.log( posibleValsToString());
        boolean posibleValsChanged = false;

        for (int n = 0; n < 27; n++)
        {
            posibleValsChanged = checkGroupForNakedTriples( groups[n]) || posibleValsChanged;
            if( posibleValsChanged )
            {
                logStrategy(7);
                return true;
            }
        }
        return false;
    }

    private boolean applyStrategy_8_naked_triples_B()
    {
//        CONSTS.log("-----------------------------   Start applyStrategy_8_naked_triples_B");
//        CONSTS.log( toString());
//        CONSTS.log( posibleValsToString());
        boolean posibleValsChanged = false;

        for (int n = 0; n < 27; n++)
        {
            posibleValsChanged = checkGroupForNakedTriplesB( groups[n]) || posibleValsChanged;
            if( posibleValsChanged ) 
            {
                logStrategy(8);
                return true;
            }
        }
        return false;
    }

    private boolean applyStrategy_9_hidden_triples()
    {
//        CONSTS.log("-----------------------------   Start applyStrategy_9_hidden_triples");
//        CONSTS.log( toString());
//        CONSTS.log( posibleValsToString());
        boolean posibleValsChanged = false;

        for (int n = 0; n < 27; n++)
        {
            posibleValsChanged = checkGroupForHiddenTriples( groups[n]) || posibleValsChanged;
            if( posibleValsChanged )
            {
                logStrategy(9);
                return true;
            }
       }
        return false;
    }

    private boolean checkGroupForHiddenTriples(int[][] group)
    {
        boolean posibleValsChanged = false;
        boolean hiddenTripleFound = false;
        ArrayList triples = getAllTriples( group);
        int[] triple = null;
        int[] found = new int[3];
        int f = 0;

        for (int t = 0; t < triples.size(); t++)
        {
            hiddenTripleFound = false;
            triple = (int[] ) triples.get(t);
            f=0;
            for (int cell = 0; cell < 9; cell++)
            {
                if( group[cell][triple[0]] +
                    group[cell][triple[1]] +    
                    group[cell][triple[2]] >= 1 )
                {
                    found[f++] = cell;
                }
                if( f == 3) break; // posible triple found
            }

            if( f == 3)  // we have to check that the triple is not in any other cell
            {
                hiddenTripleFound = true;
                for (int cell2 = 0; cell2 < 9; cell2++)
                {
                    if( cell2 == found[0] ||
                        cell2 == found[1] ||    
                        cell2 == found[2]) continue;
                    if( group[cell2][triple[0]] == 1 ||
                        group[cell2][triple[1]] == 1 ||
                        group[cell2][triple[2]] == 1 )  // is not a hidden triple
                    {
                        hiddenTripleFound = false;
                        break;
                    }
                }
            }
            
            if( hiddenTripleFound) // hidden triple found -> remove other pos vals from these 3 cells
            {
//                CONSTS.log("********* Hidden triple found: " + triple[0] + triple[1] + triple[2]);
                for (int n = 1; n <= 9; n++)
                {
                    if( triple[0] == n || 
                        triple[1] == n ||
                        triple[2] == n ) continue;
                    for (int fnd = 0; fnd < 3; fnd++)
                    {
                        
                        if( group[found[fnd]][n] == 1 )
                        {
//                            CONSTS.log("Hidden triple. Remove number: " + n);
                            group[found[fnd]][0]--;
                            group[found[fnd]][n] = 0;
                            posibleValsChanged = true;
                        }
                    }
                }
            }
            // TODO: if( posibleValsChanged) break;  (?) 
        } // for t
        return posibleValsChanged;
    }

	
	private boolean checkGroupForNakedTriples(int[][] group)
	{
		boolean posibleValsChanged = false;
		boolean found = false;
		String t1 = null, t2 = null, t3 = null;
		int posTriple1 = -1, posTriple2 = -1, posTriple3 = -1; 
		for( int triple1=0; triple1<9; triple1++)
		{
			if( group[triple1][0] != 3) continue;
			t1="";
			posTriple1 = triple1;
			for (int n = 1; n < 9; n++)
			{
				if( group[triple1][n] == 1) t1 += String.valueOf( n); 
			}
			 
			for( int triple2=triple1+1; triple2<9; triple2++)
			{
				if( group[triple2][0] != 3) continue;
				t2 = "";
				posTriple2 = triple2;
				for (int n = 1; n < 9; n++)
				{
					if( group[triple2][n] == 1) t2 += String.valueOf( n); 
				}
				if( !t1.equals( t2)) continue;
				for( int triple3=triple2+1; triple3<9; triple3++)
				{
					if( group[triple3][0] != 3) continue;
					t3 = "";
					posTriple3 = triple3;
					for (int n = 1; n < 9; n++)
					{
						if( group[triple3][n] == 1) t3 += String.valueOf( n); 
					}
					if( !t2.equals( t3)) continue;
					
					// Found!
//					CONSTS.log("Naked triple found: " + t3);
					found = true;
					break;
					
				} // for triple3
				if( found) break;
			} // for triple2
			if( found) break;
		} // for triple1

		if( found)
		{
			int n1 =   Character.getNumericValue( t1.charAt(0));
			int n2 =   Character.getNumericValue( t1.charAt(1));
			int n3 =   Character.getNumericValue( t1.charAt(2));
			for (int n = 0; n < 9; n++)
			{
				if( n==posTriple1 || n==posTriple2 || n==posTriple3 ) continue;
				if( group[n][n1] == 1 || group[n][n2] == 1 || group[n][n3] == 1 )
				{
//					CONSTS.log("naked triple. posval removed: " + 
//							(group[n][n1] == 1 ? ""+n1 : "") + 
//							(group[n][n2] == 1 ? ""+n2 : "") + 
//							(group[n][n3] == 1 ? ""+n3 : ""));
                    group[n][0] -= group[n][n1] + group[n][n2] + group[n][n3];
					group[n][n1] = group[n][n2] = group[n][n3] = 0;
					posibleValsChanged = true;
				}			
			}
		}
		return posibleValsChanged;
	}

    private boolean checkGroupForNakedTriplesB(int[][] group)
    {
        // Search groups of 3 cells with 2 or 3 numbers of the same triple.
        boolean posibleValsChanged = false;

        ArrayList triples = getAllTriples( group);
        
        int[] triple;
        int[] found = new int[3];
        int f = 0;
        for (int t = 0; t < triples.size(); t++)
        {
            triple = (int[] ) triples.get(t);
            f=0;
            for (int cell = 0; cell < 9; cell++)
            {
                if( group[cell][0] > 3) continue;
                if( group[cell][triple[0]] +  // if 3 pos vals -> all should be 1 
                    group[cell][triple[1]] +  // if 2 pos vals -> 2 of them should be 1
                    group[cell][triple[2]] == group[cell][0] )
                {
                    found[f++] = cell;
                    if(f==3) break;
                }
            }

            if( f==3) // naked triple found
            {
//                CONSTS.log("Naked triple found: " + triple[0] + triple[1] + triple[2]);
                for (int cell2 = 0; cell2 < 9; cell2++)  // remove triple from other cells
                {
                    if( cell2 == found[0] ||
                        cell2 == found[1] ||    
                        cell2 == found[2]) continue;
                    if( group[cell2][triple[0]] == 1 ||
                        group[cell2][triple[1]] == 1 ||
                        group[cell2][triple[2]] == 1 )
                    {
//                        CONSTS.log("naked triple B. posval removed: " + 
//                                (group[cell2][triple[0]] == 1 ? ""+triple[0] : "") + 
//                                (group[cell2][triple[1]] == 1 ? ""+triple[1] : "") + 
//                                (group[cell2][triple[2]] == 1 ? ""+triple[2] : ""));

                        group[cell2][0] -=  group[cell2][triple[0]] +
                                            group[cell2][triple[1]] +
                                            group[cell2][triple[2]];
                        group[cell2][triple[0]] = group[cell2][triple[1]] = group[cell2][triple[2]] = 0;
                        posibleValsChanged = true;
                    }
                }
                f=0;
            }
        }
        return posibleValsChanged;
}
    
    /**
     * @param group
     * @return
     */
    private ArrayList getAllTriples(int[][] group)
    {
        ArrayList res = new ArrayList();
        
        // Find all diferent numbers
        int[] numbers = new int[9];
        int cont = 0;
        for (int n = 1; n <= 9; n++)
        {
            for (int i = 0; i < 9; i++)
            {
                if( group[i][n] == 1)
                {
                    numbers[cont++] = n;
                    break;
                }
            }
        }
        
        // Find all posible triples with this numbers
        int[] aux = null;
        for (int t1 = 0; t1 < cont; t1++)
        {
            for (int t2 = t1+1; t2 < cont; t2++)
            {
                for (int t3 = t2+1; t3 < cont; t3++)
                {
                    aux = new int[3];
                    aux[0] = numbers[t1];
                    aux[1] = numbers[t2];
                    aux[2] = numbers[t3];
                    res.add( aux);
                }
            }
        }

        return res;
    }

    private boolean boardChanged2()
	{
    	boolean changed = false;
    	for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
           		if( posVals[i][j][0] == 1)
           		{
                    for (int k = 1; k <= 9; k++)
                    {
                        if( posVals[i][j][k] == 1) 
                        {
                            setVal( i, j, k);
                            break;
                        }
                    }
                    
//           			CONSTS.log("boardChanged: " + i + " " + j + " "
//							+ matriu[i][j] + " filled " + filled);
           			changed = true;
           		}
            }
        }
    	return changed;
	}

	private boolean removeNumFromRow(int num, int row, int except)
	{
		boolean posValsChanged = false;
//    	CONSTS.log("removeNum " + num + " FromRow " + row);
		for (int n = 0; n < 9; n++)
		{
			if( (n/3)*3 == except) continue;  // skip numbers in the same block
			if( posVals[row][n][num] == 1)
			{
				posVals[row][n][num] = 0;
				posVals[row][n][0]--;
				posValsChanged = true;
			}
//			CONSTS.log("remain: " + posVals[row][n]);
		}
//		if ( posibleValsChanged) CONSTS.log( "removeNum " + num + " FromRow " + row + posibleValsToString());
		return posValsChanged;
	}

    private boolean removeNumFromCol(int num, int col, int except)
	{
    	boolean posValsChanged = false;
//    	CONSTS.log("removeNum " + num + " FromCol " + col);
		for (int n = 0; n < 9; n++)
		{
			if( (n/3)*3 == except) continue;  // skip numbers in the same block
			if( posVals[n][col][num] == 1)
			{
				posVals[n][col][num] = 0;
				posVals[n][col][0]--;
				posValsChanged  = true;
			}
// 			CONSTS.log("remain: " + posVals[n][col]);
		}
//		if ( posValsChanged) CONSTS.log( "removeNum " + num + " FromCol " + col + posibleValsToString());
		return posValsChanged;
	}

    private boolean removeNumFromBox(int num, int i, int j, int excludeRow, int excludeCol)
	{
    	boolean posValsChanged = false;
		for(int x = i - i%3; x < i - i%3 + 3; x++)
		{
			if( x == excludeRow) continue;
			for(int y = j - j%3; y < j - j%3 + 3; y++)
			{
				if( y == excludeCol || posVals[x][y][num] == 0) continue;
				posVals[x][y][num] = 0;
				posVals[x][y][0]--;
				posValsChanged = true;
			}
		}
//		if ( posibleValsChanged) CONSTS.log( "removeNumFromBox " + posibleValsToString());
		return posValsChanged;
	}

    protected void fillPosVals()
	{
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				if (matriu[i][j] >= 1)
				{
					posVals[i][j][0] = -matriu[i][j]; 	// we store the value
														// but with minus signal
				} else
				{
					posVals[i][j][0] = 9; 				// maximum posible values
					for (int n = 1; n <= 9; n++)
					{
						posVals[i][j][n] = 1; 			 
						for (int x = 0; x < 9; x++)
						{
							if (	matriu[i][x] == n || 
									matriu[x][j] == n || 
									matriu[(i/3) * 3 + (x/3)][(j/3) * 3 + (x%3)] == n)
							{
								posVals[i][j][n] = 0; 	// no posible value
								posVals[i][j][0]--;
								break;
							}
						}
					}
				}
			}
		}
	}

    
    protected void loopAllStg()
    {
        boolean changed = true;
        Hashtable auxCache = new Hashtable();
        String key = null;
        Integer diff = null;
        while( changed && filled < 81)
        {
//            key = matriuToKey( matriu);
//            diff = (Integer )cache.get( key);
//            if( diff != null)
//            {
//                CONSTS.log("found in cache:\n" + key + " Diff: " + diff + "\n");
//                difficulty += diff.intValue();
//                if( difficulty >= 0) filled = 81;
//                break;
//            }
//            else
//            {
//                auxCache.put( key, new Integer( -difficulty));
//            }

            changed = true;
            if( !applyStrategy_1())
                if( !applyStrategy_2())
                    if( !applyStrategy_3_locked_candidates_1())
                        if( !applyStrategy_4_locked_candidates_2())
                            if( !applyStrategy_5_naked_pairs())
                                if( !applyStrategy_6_hidden_pairs())
                                    if( !applyStrategy_8_naked_triples_B())
                                        if( !applyStrategy_9_hidden_triples())
                                            changed = false;

        }
        if( filled < 81) difficulty = -10000;
//        Enumeration k = auxCache.keys(); 
//        while ( k.hasMoreElements())
//        {
//            key = (String )k.nextElement();  
//            cache.put( key, new Integer( ((Integer )auxCache.get(key)).intValue() + difficulty ));
//            //CONSTS.log("Added to cache:\n" + key + "Diff: " + cache.get(key) + "\n");
//        }
    }

	public int[][] getMatriu()
	{
		return matriu;
	}

	public int getFilled()
	{
		return filled;
	}
    
	public String toString()
	{
		return matriuToString( matriu);
	}

	public String posibleValsToString()
	{
		StringBuffer res = new StringBuffer();
		res.append("\n-----------------------------------------------------\n");
		for (int i = 0; i < 9; i++)
        {
			for (int subLine = 0; subLine < 3; subLine++)
			{
	            for (int j = 0; j < 9; j++)
	            {
	            	for (int subCol = 0; subCol < 3; subCol++)
					{
		            	int num = subLine*3 + subCol + 1;
		            	if( posVals[i][j][num] == 1)
		            	{
		            		res.append( num);
		            	}
		            	else
		            	{
		            		res.append(" ");
		            	}
					}
	            	res.append(" | ");
	            }
				res.append("\n");
			}
			res.append("-----------------------------------------------------\n");
        }
		return res.toString();
	}
	
	private boolean checkGroupForNakedPairs(int[][] group)
	{
		boolean posibleValsChanged = false;
        boolean found;
        int par1, par2;

        for (int n = 0; n < 9; n++)
        {
            if (group[n][0] == 2)
            {
                for (int m = n + 1; m < 9; m++)
                {
                    if (group[m][0] == 2)
                    {
                        found = true;
                        par1 = par2 = 0;
                        for (int k = 1; k <= 9; k++)
                        {
                            if (group[n][k] != group[m][k]) // check if is the
                                                            // same pair
                            {
                                found = false;
                                break;
                            } 
                            else if( group[n][k] == 1)
                            {
                                if( par1 == 0) par1 = k;
                                else par2 = k;
                            }
                        }
                        if ( found) // Pair found -> remove pair from other
                                    // cells
                        {
//                            CONSTS.log( "Naked pair found " + par1 + " " + par2);

                            for (int x = 0; x < group.length; x++)
                            {
                                if (x != n && x != m)
                                {
                                    if( group[x][par1] == 1)
                                    {
                                        group[x][par1] = 0;
                                        group[x][0]--;
                                        posibleValsChanged = true;
//                                        CONSTS.log( "Naked pair. pos removed: " + par1);
                                    }
                                    if( group[x][par2] == 1)
                                    {
                                        group[x][par2] = 0;
                                        group[x][0]--;
                                        posibleValsChanged = true;
//                                        CONSTS.log( "Naked pair. pos removed: " + par2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return posibleValsChanged;
	}


	/**
	 * posVals should be initialized before with method fillPosVals
	 * 
	 */
	protected int findAllSols( int level, boolean checkOnlyUninicity)
	{
		int row = -1;
		int col = -1;
		for (int i = 0; i < 9; i++)
		{ // search pos with less posible values
			for (int j = 0; j < 9; j++)
			{
				if (posVals[i][j][0] >= 0 // (we ignore solved cells)
						&& (row == -1 || posVals[i][j][0] < posVals[row][col][0]))
				{
					row = i;
					col = j;
					if (posVals[i][j][0] == 0)
						return 0; // no solution
				}
			}
		}
		if (row == -1)
		{ 
			// solution found
			// CONSTS.log( matriuToString( stats[0]));
			return 1;
		}
		int sols = 0; // number of solutions
		int numPosVals = posVals[row][col][0];
		for (int num = 1; num <= 9; num++)
		{ 
			if (posVals[row][col][num] != 1)
				continue; 
			posVals[row][col][0] = -num;
			for (int i = 0; i < 9; i++)
			{ 
				int[] aux = posVals[row][i];
				if (aux[0] >= 0 && aux[num] == 1)
				{
					aux[0]--;
					aux[num] = level;
				}
				aux = posVals[i][col];
				if (aux[0] >= 0 && aux[num] == 1)
				{
					aux[0]--;
					aux[num] = level;
				}
				aux = posVals[(row / 3) * 3 + (i / 3)][(col / 3) * 3 + (i % 3)];
				if (aux[0] >= 0 && aux[num] == 1)
				{
					aux[0]--;
					aux[num] = level;
				}
			}
			sols += findAllSols(level - 1, checkOnlyUninicity);
			for (int i = 0; i < 9; i++)
			{ 
				int[] aux = posVals[row][i];
				if (aux[0] >= 0 && aux[num] == level)
				{
					aux[0]++;
					aux[num] = 1;
				}
				aux = posVals[i][col];
				if (aux[0] >= 0 && aux[num] == level)
				{
					aux[0]++;
					aux[num] = 1;
				}
				aux = posVals[(row / 3) * 3 + (i / 3)][(col / 3) * 3 + (i % 3)];
				if (aux[0] >= 0 && aux[num] == level)
				{
					aux[0]++;
					aux[num] = 1;
				}
			}
			if ( checkOnlyUninicity && sols > 1)
			{
				break;		// with 2 or more solutions finish
			}
		}
		posVals[row][col][0] = numPosVals;
		return sols;
	}
	
    protected int calcDifficulty()
    {
        loopAllStg();
        CONSTS.log( strategiesUsed + " difficulty " + difficulty);
        return difficulty;
    }
    
    protected void resetCache()
    {
        if( cache == null) cache = new Hashtable();
        else cache.clear();
    }
}
