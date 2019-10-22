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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.gos.freesudoku.*;

/**
 *
 */
public class Board extends JFrame
{
	// Variables declaration
	protected FreeSudoku 	cParent;					// To call action for some events.
	
	private int 			cAmple 		= 322;
	private int 			cAlt 		= 465;
	
	private JPanel[][] 		cPanel 		= new JPanel[3][3];
	private Cell[][] 		cPos 		= new Cell[9][9];

	// Menu
	private JMenuItem aboutMenuItem;
	private JMenuItem contentsMenuItem;
	private JMenuItem optionsMenuItem;
	private JMenu optionsMenu;
	private JMenuItem exitMenuItem;
	private JMenu fileMenu;
	private JMenu helpMenu;
	private JMenuBar menuBar;
	private JMenuItem newMenuItem;
	private JMenuItem openMenuItem;
	private JMenuItem saveAsMenuItem;
	private JMenuItem saveMenuItem;

	// Buttons
	private JButton jButton1;
	private JButton jButton2;

	private JPanel cPanelButtons;
	private JPanel cPanelOptions;
	
	// Messages
	private JPanel cPanelMessages;
	private JTextField cTextFieldMessages;
	// End of variables declaration

    private JSlider         diffSlider;
//	private JRadioButton cRadioButtonPlay;
//	private JRadioButton cRadioButtonSolver;
	
	private static final long serialVersionUID = 1L;
	protected boolean disabled = true;
    
    private OptionsDialog optionsDialog = null;

	/**
	 * Creates new form Board 
	 */
	public Board()
	{
		initComponents();
		disableAll();
		setMessage("Click the START Button!");
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents()
	{
		setTitle("Free Sudoku");
		getContentPane().setLayout(new FlowLayout());
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - cAmple) / 2, (screenSize.height - cAlt) / 2,
				cAmple, cAlt);

		// Buttons & Messages
		cPanelButtons = new JPanel();
		cPanelOptions = new JPanel();
		jButton1 = new JButton();
		jButton2 = new JButton();
		cPanelMessages = new JPanel();
		cTextFieldMessages = new JTextField();

		// Menus
		menuBar = new JMenuBar();
		fileMenu = new JMenu();
		newMenuItem = new JMenuItem();
		openMenuItem = new JMenuItem();
		saveMenuItem = new JMenuItem();
		saveAsMenuItem = new JMenuItem();
		exitMenuItem = new JMenuItem();
		optionsMenu = new JMenu();
		optionsMenuItem = new JMenuItem();
		helpMenu = new JMenu();
		contentsMenuItem = new JMenuItem();
		aboutMenuItem = new JMenuItem();

		cPanelButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
		cPanelOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		cPanelButtons.setPreferredSize(new Dimension(143, 50));
		cPanelOptions.setPreferredSize(new Dimension(142, 60));
		jButton1.setFont(new Font("MS Sans Serif", Font.BOLD, 12));
		jButton1.setForeground( CONSTS.GREEN_DARK);
		jButton1.setText("Start");
		jButton1.setToolTipText("Start a new Sudoku Game");
		//jButton1.setPreferredSize(new Dimension(35, 35));
		cPanelButtons.add(jButton1);
		jButton2.setText("Stop");
		jButton2.setForeground( CONSTS.RED_DARK);
		jButton2.setToolTipText("Cancel current Game");
//		jButton2.setPreferredSize(new Dimension(35, 35));
		cPanelButtons.add(jButton2);
//		jButton3.setText("B");
//		jButton3.setPreferredSize(new Dimension(35, 35));
//		cPanelButtons.add(jButton3);
//		jButton4.setText("C");
//		jButton4.setPreferredSize(new Dimension(35, 35));
//		cPanelButtons.add(jButton4);

        diffSlider = new JSlider(JSlider.HORIZONTAL, 1, 4, 2);
        diffSlider.setMajorTickSpacing(1);
        diffSlider.setMinorTickSpacing(1);
        diffSlider.setPaintTicks(true);
        diffSlider.setPaintLabels(true);
        diffSlider.setSnapToTicks( true);
        diffSlider.setPreferredSize( new Dimension(100, 45));
        diffSlider.setToolTipText("Difficulty level");

		cPanelOptions.add( diffSlider);
		getContentPane().add(cPanelButtons);
		getContentPane().add(cPanelOptions);
		
		jButton1.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
                cParent.restart();
			}
		});
		jButton2.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
                disableAll();
			}
		});

        Cell.board = this;      // Inicialitza referencia a les caselles.
		// Init each 3x3 panel
		for (int px = 0; px < 3; px++)
		{
			for (int py = 0; py < 3; py++)
			{
				cPanel[px][py] = new JPanel();
				initPanel( cPanel[px][py]);

				// Init each position from panel
				for( int cx = 0; cx < 3; cx++)
				{
					for( int cy = 0; cy < 3; cy++)
					{
						int i = px*3 + cx;
						int j = py*3 + cy;
						cPos[i][j] = new Cell( i, j);
						cPanel[px][py].add( cPos[i][j]);
					}
				}

				// Add this 3x3 panel
				getContentPane().add( cPanel[px][py]);
			}
		}

		// Messages
		cPanelMessages.setPreferredSize(new Dimension(280, 50));
		cTextFieldMessages.setBackground(new Color(204, 204, 204));
		cTextFieldMessages.setPreferredSize(new Dimension(280, 40));
		cTextFieldMessages.setHorizontalAlignment(JTextField.CENTER);
		cTextFieldMessages.setFont(new Font("MS Sans Serif", Font.BOLD, 14));
		cTextFieldMessages.setForeground( Color.BLUE);
		cPanelMessages.add(cTextFieldMessages);

		getContentPane().add(cPanelMessages);

		// Menus
        fileMenu.setText("File");
		newMenuItem.setText("New Game");
		fileMenu.add(newMenuItem);
		openMenuItem.setText("Open");
		fileMenu.add(openMenuItem);
		saveMenuItem.setText("Save");
		fileMenu.add(saveMenuItem);
		saveAsMenuItem.setText("Save As ...");
		fileMenu.add(saveAsMenuItem);
		exitMenuItem.setText("Exit");
		fileMenu.add(exitMenuItem);
		menuBar.add(fileMenu);
		optionsMenu.setText("Options");
		optionsMenuItem.setText("Options");
		optionsMenu.add(optionsMenuItem);
		menuBar.add(optionsMenu);
		helpMenu.setText("Help");
		contentsMenuItem.setText("Contents");
		helpMenu.add(contentsMenuItem);
		aboutMenuItem.setText("About");
		helpMenu.add(aboutMenuItem);
		menuBar.add(helpMenu);

        newMenuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent evt)
                    {
                        cParent.restart();
                    }
                });
        exitMenuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent evt)
                    {
                        exitMenuItemActionPerformed(evt);
                    }
                });
        optionsMenuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent evt)
                    {
                        if( optionsDialog == null)
                            optionsDialog = new OptionsDialog();
                        optionsDialog.show();
                    }
                });
        aboutMenuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent evt)
                    {
                        JOptionPane.showMessageDialog( 
                                getContentPane(), 
                                "FreeSudoku Project\n" +
                                "version " + CONSTS.version + "\n" +
                                "freesudoku.sourceforge.net\n\n" +
                                "Victorf2 (at) users.sourceforge.net\n" +
                                "GNU GPL License",
                                "About",
                                JOptionPane.PLAIN_MESSAGE                                
                                );
                    }
                });
        contentsMenuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent evt)
                    {
                        JOptionPane.showMessageDialog( 
                                getContentPane(), 
                                "Free Sudoku\n\n" +
                                "Doble-click: Enter more than one digit.\n" +
                                "CTRL-click: Display posibles values.\n" +
                                "\nGame Info:\n" + 
                                " English -> http://en.wikipedia.org/wiki/Sudoku\n" +
                                " Spanish -> http://es.wikipedia.org/wiki/Sudoku\n"
                                ,
                                
                                "Help",
                                JOptionPane.PLAIN_MESSAGE                                
                                );
                    }
                });
        
        
		openMenuItem.setEnabled( false);
		saveMenuItem.setEnabled( false);
		saveAsMenuItem.setEnabled( false);
        optionsMenuItem.setEnabled( false);
		setJMenuBar(menuBar);

		// CONSTS.log("End initComponents()!");
	}

	private void initPanel(JPanel panel)
	{
		panel.setLayout(new GridLayout(3, 3, 1, 1));
		panel.setBorder(new EtchedBorder());
		panel.setPreferredSize(new Dimension(93, 93));
		panel.setRequestFocusEnabled(false);
	}


	private void exitMenuItemActionPerformed(ActionEvent evt)
	{
		System.exit(0);
	}

	public void setPosValue( int i, int j, int val)
	{
		cPos[i][j].setValue( val);
	}
	
	private void setPos( int[][] pos, boolean[][] pInitPos)
	{
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
                cPos[i][j].setNormalText();
				cPos[i][j].setBackground( CONSTS.BLUE_CLEAR);
				if( pInitPos[i][j])
				{
                    cPos[i][j].initPos = true;
					cPos[i][j].setText( String.valueOf( pos[i][j]));
					cPos[i][j].setForeground( Color.BLACK);
				}
				else
				{
                    cPos[i][j].initPos = false;
					cPos[i][j].setText("");
					cPos[i][j].setForeground( Color.BLUE);
				}
			}
			
		}
	}

	public void initPartida(int[][] pos, boolean[][] pInitPos)
	{
		setMessage("");
		setPos( pos, pInitPos);
		jButton2.setEnabled( true);
		jButton1.setEnabled( false);
		newMenuItem.setEnabled( false);
		disabled = false;
		diffSlider.setEnabled( false);
		optionsMenuItem.setEnabled( false);
	}

	public void setParent(FreeSudoku parent)
	{
		cParent = parent;
	}

	public void setMessage( String pMessage)
	{
		cTextFieldMessages.setText( pMessage);
	}

	public int getNivel()
	{

		return diffSlider.getValue();
	}

	public void disableAll()
	{
		cTextFieldMessages.setText("");
		jButton1.setEnabled( true);
		jButton2.setEnabled( false);
		newMenuItem.setEnabled( true);
		disabled  = true;
		setPosBackgroundColor( CONSTS.GRAY_CLEAR);
		diffSlider.setEnabled( true);
//		optionsMenuItem.setEnabled( true);
	}

	private void setPosBackgroundColor(Color pColor)
	{
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				cPos[i][j].setBackground( pColor);
			}
		}
	}

    /**
     * @return
     */
    public boolean isSymmetric()
    {
        if( optionsDialog == null)
            optionsDialog = new OptionsDialog();
        return optionsDialog.isSymmetric();
    }
    
    protected String getPosSmall( int i, int j)
    {
        return cPos[i][j].getPosSmall();
    }

    /**
     * Check if everything is still ok in the area of influenve of one cell, only multivalue cell.
     */
    public void checkConsistence( int i, int j)
    {
        if( cPos[i][j].isSmall) CONSTS.log("ERROR: checkConsistence. Shouldn't be small");
        String valInCell = cPos[i][j].getText();
        if( valInCell.equals("")) return;
        for(int x = 0; x < 9; x++)
        {
            if( cPos[i][x].isSmall && cPos[i][x].getText().indexOf( valInCell) != -1)
                setAlertInCell( i, x);
            if( cPos[x][j].isSmall  && cPos[x][j].getText().indexOf( valInCell) != -1)
                setAlertInCell( x, j);
        }

        for(int x = i - i%3; x < i - i%3 + 3; x++)
        {
            for(int y = j - j%3; y < j - j%3 + 3; y++)
            {
                if( cPos[x][y].isSmall && cPos[x][y].getText().indexOf( valInCell) != -1)
                    setAlertInCell( x, y);
            }
        }

        
    }
    
    private void setAlertInCell( int i, int j)
    {
        cPos[i][j].setBackground( CONSTS.RED_CLEAR);
        setMessage( "One optional value in cell is not consistent!");
    }
}
