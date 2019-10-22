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
package org.gos.freesudoku.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;

import org.gos.freesudoku.CONSTS;

/**
 * @author gos
 *
 */
public class Cell extends JTextField
{
    private static final long serialVersionUID = 1L;

    private final int   i;              // Coordenades inicials
    private final int   j;
    
    protected boolean		initPos = false;    // Si es va mostrar inicialment
    public static Board		board  = null;     	// Referencia per comunicarse amb el board

    private static Cell		currPos  = null;
    protected boolean 		isSmall  = false;
    
    
    /**
     * @param i
     * @param j
     */
    public Cell(final int i, final int j)
    {
        super();
        this.i = i;
        this.j = j;
        initCasella();
    }
    
    private void initCasella()
    {
        setEditable(false);
        setForeground( Color.BLUE);
        setHorizontalAlignment(JTextField.CENTER);
        setNormalText();

        this.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent evt) {
                casellaMouseClicked( evt);
            }

        });
        this.addKeyListener( new KeyAdapter(){
            public void keyTyped( KeyEvent evt) {
                casellaKeyTyped( evt);
            }
        });
    }
    
    private void casellaKeyTyped(KeyEvent evt)
    {
        board.setMessage("");
        if(     currPos == null || board.disabled ||
                ((evt.getKeyChar() < '1' || evt.getKeyChar() > '9') && evt.getKeyChar() != ' ') ) 
            return;
        char charPressed = evt.getKeyChar() == ' ' ? '0' : evt.getKeyChar();
        if ( charPressed >= '0' && charPressed <= '9')
        {
            int val = new Integer( String.valueOf( charPressed)).intValue();
            if( isSmall)
            	board.cParent.posKeyTypedSmall( i, j, val);
            else
            	board.cParent.posKeyTyped( i, j, val);
        }
    }
    
    private void casellaMouseClicked(MouseEvent evt)
    {
        board.setMessage("");
        if( board.disabled) return;
        if( currPos != null)
        {
        	if( currPos.getText().length() == 0)  // If user types nothing -> back to normal
        	{
        		currPos.isSmall = false;
        		currPos.setNormalText();
        	}
        	currPos.setBgColorNormal();
        }
        	
        if( !initPos) 
        {
        	if( evt.getClickCount() == 2) 
        	{
        		isSmall = !isSmall;
        		if( isSmall)
                {
                    board.cParent.posKeyTyped( i, j, 0);  // Maybe there was a value
        			setSmallText();
                }
        		else
        			setNormalText();
        	}
    		setBgColorEdit();
            currPos = this;
            if( evt.isControlDown())
                board.setMessage( board.cParent.getPosibleVals( i, j));
        }
        else
        {
            currPos = null;
        }
        requestFocusInWindow();
    }
    
    public void setValue( int val)
    {
    	if( isSmall)
    	{
    		if( val == 0)
    		{
    			setText("");
    		}
    		else
    		{
    			String curr = getText();
    			String digit = String.valueOf( val);
    			if( curr.indexOf( digit) != -1 ) return;  // digit already typed
    			if( curr.length() == 4)  // if maximum length -> rotation
    			{
    				setText( curr.substring( 1) + digit);
    			} else
    			{
    				setText( curr + digit);
    			}
    		}
    	}
    	else
    	{
            setText( val == 0 ? "" : String.valueOf( val));
    	}
    }
    
    protected void setSmallText()
    {
        setFont(new Font("MS Sans Serif", Font.PLAIN, 10));
        setBackground( Color.YELLOW);
    }

    public void setNormalText()
    {
        setFont(new Font("MS Sans Serif", Font.BOLD, 18));
        setBackground( CONSTS.BLUE_CLEAR);
//        if( getText().length() > 1) setText( getText().substring(0,1));
        setText("");  // Clean up
    }
    
    private void setBgColorEdit()
    {
		if( isSmall)
			setBackground( Color.YELLOW);
		else
			setBackground( new Color(252, 252, 252));
    }
    private void setBgColorNormal()
    {
		if( isSmall)
			setBackground( Color.YELLOW);
		else
			setBackground( CONSTS.BLUE_CLEAR);
    	
    }
    
    protected String getPosSmall()
    {
        if( isSmall) return getText();
        return null;
    }
}
