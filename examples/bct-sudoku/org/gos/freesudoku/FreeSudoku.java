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

import org.gos.freesudoku.view.Board;

public class FreeSudoku
{
	private Board 	board;
	private Game 	game;

	private FreeSudoku()
	{
		// Initialitations
		board = new Board();
		board.setParent( this);
		board.setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new FreeSudoku();
	}

	public void posKeyTyped( int i, int j, int val)
	{
		if( game.isLegal( i, j, val))
		{
			board.setPosValue( i, j, val);
			game.setPosVal( i, j, val);
			if( game.resolved()) solved();
            if( val != 0) board.checkConsistence( i, j);
		}
		else
		{
			board.setMessage("Number already used!");
		}
	}

	public void posKeyTypedSmall( int i, int j, int val)
	{
		// TODO: check this and prev method
		if( game.isLegal( i, j, val))
		{
			board.setPosValue( i, j, val);
		}
		else
		{
			board.setMessage("Number already used!");
		}
	}

	public String getPosibleVals(int i, int j)
	{
		StringBuffer aux = new StringBuffer();
		ArrayList al = game.getAllLegalVals( i, j);
		for (int k = 0; k < al.size(); k++)
		{
			aux.append(al.get(k) + " ");
		}
		
		return aux.toString().trim();
	}

	private void solved()
	{
		board.disableAll();
		board.setMessage("Congratulations!");
	}

	public void restart()
	{
		game = new Game();
		game.initPartida( board.getNivel(), board.isSymmetric());
		board.initPartida( game.getPos(), game.getInitPos());
		board.setParent( this);
		board.setVisible(true);
	}

	public String partidaToString()
	{
		return game.toString();
	}
}
