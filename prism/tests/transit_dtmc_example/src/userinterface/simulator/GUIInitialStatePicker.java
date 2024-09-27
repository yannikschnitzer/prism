//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Andrew Hinton <ug60axh@cs.bham.ac.uk> (University of Birmingham)
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford, formerly University of Birmingham)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package userinterface.simulator;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import parser.State;
import parser.Values;
import parser.ast.Expression;
import prism.ModelGenerator;
import prism.Prism;
import prism.PrismException;
import prism.PrismLangException;
import simulator.SimulatorEngine;
import userinterface.GUIPrism;

@SuppressWarnings("serial")
public class GUIInitialStatePicker extends javax.swing.JDialog implements KeyListener
{
	//STATICS    
	public static final int NO_VALUES = 0;
	public static final int VALUES_DONE = 1;
	public static final int CANCELLED = 2;

	//ATTRIBUTES    
	private boolean cancelled = true;

	private JTable initValuesTable;
	private DefineValuesTable initValuesModel;

	private GUIPrism gui;
	private ModelGenerator<Double> modelGen;
	private int numVars;
	
	private State initialState;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel allPanel;
	private javax.swing.JPanel bottomPanel;
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JButton cancelButton;
	private javax.swing.JPanel innerPanel;
	private javax.swing.JButton okayButton;
	private javax.swing.JCheckBox optionCheckBox;
	private javax.swing.JPanel topPanel;

	// End of variables declaration//GEN-END:variables

	/** Creates new form GUIConstantsPicker */
	public GUIInitialStatePicker(GUIPrism parent, SimulatorEngine engine, Values initialStateValues)
	{
		super(parent, "Initial State for Simulation", true);

		this.gui = parent;
		this.modelGen = engine.getModel();
		this.numVars = modelGen.getNumVars();

		//setup tables
		initValuesModel = new DefineValuesTable();
		initValuesTable = new JTable();
		initValuesTable.setModel(initValuesModel);
		initValuesTable.setSelectionMode(DefaultListSelectionModel.SINGLE_INTERVAL_SELECTION);
		initValuesTable.setCellSelectionEnabled(true);
		initValuesTable.setRowHeight(getFontMetrics(initValuesTable.getFont()).getHeight() + 4);

		//initialise
		initComponents();
		this.getRootPane().setDefaultButton(okayButton);
		initTable();
		initValues(initialStateValues);

		super.setBounds(new Rectangle(550, 300));
		setResizable(true);
		setLocationRelativeTo(getParent()); // centre
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
	private void initComponents()
	{
		allPanel = new javax.swing.JPanel();
		bottomPanel = new javax.swing.JPanel();
		buttonPanel = new javax.swing.JPanel();
		okayButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		optionCheckBox = new javax.swing.JCheckBox();
		topPanel = new javax.swing.JPanel();
		innerPanel = new javax.swing.JPanel();

		addWindowListener(new java.awt.event.WindowAdapter()
		{
			public void windowClosing(java.awt.event.WindowEvent evt)
			{
				closeDialog(evt);
			}
		});

		allPanel.setLayout(new java.awt.BorderLayout());

		allPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
		bottomPanel.setLayout(new java.awt.BorderLayout());

		buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

		okayButton.setText("Okay");
		okayButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				okayButtonActionPerformed(evt);
			}
		});

		buttonPanel.add(okayButton);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				cancelButtonActionPerformed(evt);
			}
		});

		buttonPanel.add(cancelButton);

		bottomPanel.add(buttonPanel, java.awt.BorderLayout.EAST);

		optionCheckBox.setText("Always prompt for initial state on path creation");
		optionCheckBox.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				optionCheckBoxActionPerformed(evt);
			}
		});

		//bottomPanel.add(optionCheckBox, java.awt.BorderLayout.WEST);
		optionCheckBox.getAccessibleContext().setAccessibleName("optionCheckBox");

		allPanel.add(bottomPanel, java.awt.BorderLayout.SOUTH);

		topPanel.setLayout(new java.awt.BorderLayout());

		topPanel.setBorder(new javax.swing.border.TitledBorder("Initial state"));
		innerPanel.setLayout(new java.awt.BorderLayout());

		innerPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
		topPanel.add(innerPanel, java.awt.BorderLayout.CENTER);

		allPanel.add(topPanel, java.awt.BorderLayout.CENTER);

		getContentPane().add(allPanel, java.awt.BorderLayout.CENTER);

	}

	// </editor-fold>//GEN-END:initComponents

	private void optionCheckBoxActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_optionCheckBoxActionPerformed
	// TODO add your handling code here:
	}//GEN-LAST:event_optionCheckBoxActionPerformed

	private void initTable()
	{
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(initValuesTable);
		innerPanel.add(sp);
		innerPanel.setPreferredSize(new Dimension(300, 300));
	}

	private void initValues(Values initialStateValues)
	{
		try {
			boolean match = false;
			// If values have been passed in, see if they match the model
			// And if so, store them in the table
			if (initialStateValues != null) {
				match = true;
				if (initialStateValues.getNumValues() != numVars) {
					match = false;
				} else {
					for (int i = 0; i < numVars; i++) {
						String varName = modelGen.getVarName(i);
						if (!initialStateValues.contains(varName)) {
							match = false;
							break;
						} else {
							try {
								Object value = initialStateValues.getValueOf(varName);
								value = modelGen.getVarType(i).castValueTo(value);
								initValuesModel.setValue(i, value);
							} catch (PrismLangException e) {
								match = false;
							}
						}
					}
				}
			}
			// Otherwise get default initial state for model and store in the table
			if (!match) {
				State defaultInitialState = modelGen.getInitialState();
				for (int i = 0; i < numVars; i++) {
					initValuesModel.setValue(i, defaultInitialState.varValues[i]);
				}
			}
		} catch (PrismException e) {
			gui.errorDialog("Error choosing initial state: " + e.getMessage());
		}
	}

	/** Call this static method to construct a new GUIValuesPicker to define
	 *  initialState.  If you don't want any default values, then pass in null for
	 *  initDefaults
	 */
	public static State defineInitalValuesWithDialog(GUIPrism parent, SimulatorEngine engine, Values initDefaults)
	{
		return new GUIInitialStatePicker(parent, engine, initDefaults).defineValues();
	}

	public State defineValues()
	{
		setVisible(true);
		if (cancelled)
			return null;
		else
			return initialState;
	}

	private void okayButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okayButtonActionPerformed
	{//GEN-HEADEREND:event_okayButtonActionPerformed
		if (initValuesTable.getCellEditor() != null) {
			initValuesTable.getCellEditor().stopCellEditing();
		}

		String varName = "";
		String valueString = "";
		try {
			State newInitialState = new State(numVars);
			// check each variable value
			for (int i = 0; i < numVars; i++) {
				varName = modelGen.getVarName(i);
				valueString = initValuesModel.getValue(i).toString();
				Expression valueExpr = Prism.parseSingleExpressionString(valueString);
				if (!valueExpr.isConstant()) {
					throw new PrismException("Not constant");
				}
				Object valueObj = valueExpr.evaluate();
				if (!modelGen.getVarType(i).canCastTypeTo(valueExpr.getType())) {
					throw new PrismException("Type mismatch");
				}
				valueObj = modelGen.getVarType(i).castValueTo(valueObj);
				newInitialState.varValues[i] = valueObj;
			}
			initialState = newInitialState;
			cancelled = false;
			dispose();
		} catch (PrismException e) {
			gui.errorDialog("Invalid value \"" + valueString + "\" for variable " + varName + ": " + e.getMessage());
		}
	}//GEN-LAST:event_okayButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
	{//GEN-HEADEREND:event_cancelButtonActionPerformed
		dispose();
	}//GEN-LAST:event_cancelButtonActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
	{
		setVisible(false);
		dispose();
	}//GEN-LAST:event_closeDialog

	public void keyPressed(KeyEvent e)
	{
	}

	public void keyReleased(KeyEvent e)
	{

	}

	public void keyTyped(KeyEvent e)
	{

	}

	class DefineValuesTable extends AbstractTableModel
	{
		Object[] values;

		public DefineValuesTable()
		{
			values = new Object[numVars];
		}

		public void setValue(int i, Object value)
		{
			values[i] = value;
			fireTableRowsInserted(i, i);
		}

		public Object getValue(int i)
		{
			return values[i];
		}

		public int getColumnCount()
		{
			return 3;
		}

		public int getRowCount()
		{
			return numVars;
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			try {
				switch (columnIndex) {
				case 0:
					return modelGen.getVarName(rowIndex);
				case 1:
					return modelGen.getVarType(rowIndex);
				case 2:
					return values[rowIndex].toString();
				default:
					return "";
				}
			} catch (PrismException e) {
				return "";
			}
		}

		public String getColumnName(int columnIndex)
		{
			switch (columnIndex) {
			case 0:
				return "Name";
			case 1:
				return "Type";
			case 2:
				return "Value";
			default:
				return "";
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			if (columnIndex == 2)
				return true;
			else
				return false;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			if (columnIndex == 2) {
				values[rowIndex] = aValue;
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}

		public String toString()
		{
			String str = "";
			for (int i = 0; i < numVars; i++) {
				str += modelGen.getVarName(i) + "=" + values[i].toString();
				if (i < numVars - 1) {
					str += ",";
				}
			}
			return str;
		}
	}
}
