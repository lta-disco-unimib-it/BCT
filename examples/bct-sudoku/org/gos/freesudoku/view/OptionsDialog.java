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

import java.awt.*;

import javax.swing.*;

public class OptionsDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    private int 			cAmple 			= 240;
	private int 			cAlt 			= 300;
	
	private JLabel			symmLabel		= null;
	private JCheckBox		symmCheckBox	= null;
	private JLabel			trainnigLabel	= null;
	private JCheckBox 		trainnigCheckBox;
	
	public OptionsDialog()
	{
		setTitle("Options");
		setModal( true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - cAmple) / 2, (screenSize.height - cAlt) / 2,
				cAmple, cAlt);
		GridLayout grid = new GridLayout(5,2);
		grid.setHgap( 10);
		grid.setVgap( 10);
		getContentPane().setLayout( grid);
		
		
		initComps();
		
	}

	private void initComps()
	{
				
		symmLabel = initLabel( "Symmetric Game:");
		symmCheckBox = new JCheckBox();
        symmCheckBox.setSelected( true);
		getContentPane().add( symmCheckBox);
        
		
		trainnigLabel = initLabel( "Trainning mode:");
		trainnigCheckBox = new JCheckBox();
        trainnigCheckBox.setSelected( false);
		getContentPane().add( trainnigCheckBox);
	}

	private JLabel initLabel(String pText)
	{
		JLabel label = new JLabel( pText);
		label.setPreferredSize( new Dimension(150, 30));
		label.setHorizontalAlignment( JLabel.RIGHT);
		getContentPane().add( label);
		return label;
	}

	private JTextField initTextField(String pText)
	{
		JTextField field = new JTextField( pText);
		field.setEditable( false);
		field.setBackground( new Color(200,240,250));
		field.setPreferredSize( new Dimension(100, 20));
		getContentPane().add( field);
		return field;
	}
	
    public boolean isTrainnigMode()
    {
        return trainnigCheckBox.isSelected();
    }
    public boolean isSymmetric()
    {
        return symmCheckBox.isSelected();
    }
}
