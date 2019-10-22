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

public class Game
{
	private int[][] 	cPos 			= null;		// posicion en la partida.
	private int[][] 	cRes 			= null;		// posicion del resultado.
	private boolean[][]	cInitPos		= null;		// posiciones mostradas inicialmente.
//	private int			cNivel 			= -50;
	private int			cNivel 			= 5;
	private int			cPosFilled		= 0;
	private Random 		cGenerator		= null;
	private boolean		isSymmetric		= true;

	protected Game()
	{
		cPos = new int[9][9];
		cRes = new int[9][9];
		cInitPos = new boolean[9][9];
		cGenerator = new Random();
	}

	protected void initPartida(int pNivel, boolean pIsSymmetric)
	{
		cNivel = pNivel;
		isSymmetric = pIsSymmetric;
		Solver solver = new Solver();
		// int numRes = 0;
		int maxDiff = -1;
        int maxRes[][] = null;
        int maxPos[][] = null;
        boolean found = false;
        
		for (int part = 0; part < 2+cNivel/2; part++)
        {
            clearArray(cRes);
            generatePartida(0);
            for (int pos = 0; pos < 2+cNivel*2; pos++)
            {
                clearArray(cPos);
                generateInitPos();
//                CONSTS.log(toString());
//                CONSTS.log(arrayToString(cPos));

                solver.setMatriu(cPos);
                solver.loopAllStg();
                CONSTS.log(solver.strategiesUsed);
                CONSTS.log("initPartida Diff: " + solver.difficulty);
                if( cNivel == 1)
                {
                    if( solver.difficulty >=  0 && solver.difficulty < 10)
                    {
                        maxDiff = solver.difficulty;
                        maxRes = Solver.dupArray( cRes);
                        maxPos = Solver.dupArray( cPos);
                        found = true;
                        break;
                    }
                    if( (maxDiff == -1) || Math.abs( maxDiff-0) > Math.abs(solver.difficulty-0))
                    {
                        maxDiff = solver.difficulty;
                        maxRes = Solver.dupArray( cRes);
                        maxPos = Solver.dupArray( cPos);
                    }
                }

                if( cNivel == 2)
                {
                    if( solver.difficulty >= 10 && solver.difficulty < 30)
                    {
                        maxDiff = solver.difficulty;
                        maxRes = Solver.dupArray( cRes);
                        maxPos = Solver.dupArray( cPos);
                        found = true;
                        break;
                    }
                    if( (maxDiff == -1) || Math.abs( maxDiff-25) > Math.abs(solver.difficulty-25))
                    {
                        maxDiff = solver.difficulty;
                        maxRes = Solver.dupArray( cRes);
                        maxPos = Solver.dupArray( cPos);
                    }
                }

                if( cNivel == 3)
                {
                    if( solver.difficulty >= 20 && solver.difficulty < 50)
                    {
                        maxDiff = solver.difficulty;
                        maxRes = Solver.dupArray( cRes);
                        maxPos = Solver.dupArray( cPos);
                        found = true;
                        break;
                    }
                    if( (maxDiff == -1) || Math.abs( maxDiff-45) > Math.abs(solver.difficulty-45))
                    {
                        maxDiff = solver.difficulty;
                        maxRes = Solver.dupArray( cRes);
                        maxPos = Solver.dupArray( cPos);
                    }
                }

                if( cNivel == 4 )
                {
                    if( maxDiff < solver.difficulty)
                    {
                        maxDiff = solver.difficulty;
                        maxRes = Solver.dupArray( cRes);
                        maxPos = Solver.dupArray( cPos);
                    }
                }
                if( found) break;
            }
            if( found) break;
        }
        CONSTS.log(" * * * * * * * * * * * * maxDiff: " +  maxDiff);
        if (maxRes != null)
        {
            cRes = maxRes;
            cPos = maxPos;
            for (int i = 0; i < 9; i++)
            {
                for (int j = 0; j < 9; j++)
                {
                    cInitPos[i][j] = (cPos[i][j] != 0);
                }
            }
        }

	}
	
    private void generateInitPos()
    {
        int maxPosToFill = 33 - cNivel*3;

        // fill up
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                cPos[i][j] = cRes[i][j];
                cInitPos[i][j] = true;
            }
        }
        cPosFilled = 81;

        boolean[] used = new boolean[81];
        int usedCount = 81;
        Arrays.fill( used, false);
        Solver solver = new Solver();
        while (cPosFilled > maxPosToFill && usedCount > 1)
        {
            int i = cGenerator.nextInt( 81);
            do
            {
                if( i < 80 ) i++;
                else i = 0;
            } while( used[i]);
            
            // CONSTS.log("trying " + i/9 + "-" + i%9 + " : " + (8-i/9) + "-" + (8 - i%9));
            used[i] = true;
            usedCount--;
            if( isSymmetric)
            {
                used[ 9*(8-i/9) + (8 - i%9) ] = true;
                usedCount--;
            }
            clearPar( i/9, i%9);
            solver.setMatriu( cPos);
            solver.fillPosVals();
            int sols = solver.findAllSols( -1, true);
            if( sols > 1)
            {
                initOneParMore( i/9, i%9);
            }
            
//            solver.loopAllStg();
//            CONSTS.log("cPosFilled: " + cPosFilled + " sols: " + sols + " diff: " + solver.difficulty);
        }
    }

    /**
     */
    private void clearPar(int i, int j)
    {
        if( cPos[i][j] == 0) return;
        cPos[i][j] = 0;
        cInitPos[i][j] = false;
        cPosFilled--;
        if (isSymmetric && (i != 4 || j != 4 ) && cInitPos[8 - i][8 - j])
        {
            cPos[8 - i][8 - j] = 0;
            cInitPos[8 - i][8 - j] = false;
            cPosFilled--;
        }
        
    }

    private void initOneParMore( int i, int j)
    {
    	if( cPos[i][j] != 0) return;
		cPos[i][j] = cRes[i][j];
		cInitPos[i][j] = true;
		cPosFilled++;

		if (isSymmetric && (i != 4 || j != 4 ) && !cInitPos[8 - i][8 - j])
		{
			cPos[8 - i][8 - j] = cRes[8 - i][8 - j];
			cInitPos[8 - i][8 - j] = true;
			cPosFilled++;
		}
    }

    private boolean generatePartida(int pPos)
	{
		// CONSTS.log( taulerToString( cRes) + "\n");
		// CONSTS.log( pPos);
		if( pPos == 9*9 ) return true;
		ArrayList posibleVals = getAllCorrectVals( pPos/9, pPos%9);
		if( posibleVals.isEmpty()) return false;
		
		while( !posibleVals.isEmpty())
		{
			int candidate = cGenerator.nextInt( posibleVals.size());
			cRes[pPos/9][pPos%9] = ((Integer )posibleVals.get( candidate)).intValue();
			if( isCorrect( pPos/9, pPos%9, cRes[pPos/9][pPos%9]))
			{
				if( generatePartida( pPos + 1)) 
				{
					return true;
				}
			}
			posibleVals.remove( candidate);
		}
		cRes[pPos/9][pPos%9] = 0;
		// CONSTS.log(" --------- ");
		return false;
	}
	
	protected ArrayList getAllLegalVals( int i, int j)
	{
		return getCorrectValsForArray( i, j, cPos);
	}

	private ArrayList getAllCorrectVals( int i, int j)
	{
		return getCorrectValsForArray( i, j, cRes);
	}

	protected static ArrayList getCorrectValsForArray( int i, int j, int[][] pArray)
	{
		ArrayList res = new ArrayList();
        if( pArray[i][j] != 0) return res;
		for (int val = 1; val <= 9; val++)
		{
			if( isCorrect( i, j, val, pArray))
				res.add( new Integer( val));
		}
		return res;
	}

	private void clearArray( int[][] pArray)
	{
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				pArray[i][j] = 0;
			}
		}
	}

	protected int[][] getPos()
	{
		return cPos;
	}

	protected boolean isLegal( int i, int j, int val)
	{
		if( val == 0) return true;
		return isCorrect( i, j, val, cPos);
	}
	
	private boolean isCorrect( int i, int j, int val)
	{
		if( val == 0) return false;
		return isCorrect( i, j, val, cRes);
	}

	protected static boolean isCorrect( int i, int j, int val, int[][] pArray)
	{
		// CONSTS.log("isCorrect() " + i + " " + j + " " + val);
		for(int x = 0; x < 9; x++)
		{
			if( pArray[i][x] == val && x != j) return false;
			if( pArray[x][j] == val && x != i) return false;
		}
		
		for(int x = i - i%3; x < i - i%3 + 3; x++)
		{
			for(int y = j - j%3; y < j - j%3 + 3; y++)
			{
				if( pArray[x][y] == val && ( x != i || y != j)) return false;
			}
		}
		
		return true;
	}

	protected String arrayToString( int[][] pArray)
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

	protected void setPosVal(int i, int j, int val)
	{
		if( 		cPos[i][j] == 0 && val != 0) cPosFilled++;
		else if( 	cPos[i][j] != 0 && val == 0) cPosFilled--;
		cPos[i][j] = val;
		// CONSTS.log("cPosFilled " + cPosFilled);
	}

	protected boolean[][] getInitPos()
	{
		return cInitPos;
	}

	protected boolean resolved()
	{
		return cPosFilled == 9*9;
	}
	
	// for testing
	public String toString()
	{
		return arrayToString( cRes);
	}

}
