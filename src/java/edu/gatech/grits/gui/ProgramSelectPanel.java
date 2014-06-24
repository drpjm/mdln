package edu.gatech.grits.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;

import edu.gatech.grits.gui.model.*;
import edu.gatech.grits.mdln.lang.util.MDLnProgram;

import javolution.util.*;

public class ProgramSelectPanel extends MyPanel {

	private final String USER_DIR = System.getProperty("user.dir");
	
	private JButton chooseButton;
	private JButton compileButton;
	private JButton cancelButton;

	private CompilerBackend compiler;

	private File mdlnFile;
	//MDLn data
	private MDLnProgram currProgram;

	public ProgramSelectPanel(CompilerBackend c){
		this.observers = new FastList<PanelObservable>();
		this.compiler = c;

		buildContent();
		buildLayout();
	}

	@Override
	protected final void buildContent() {

		chooseButton = new JButton("Load File");
		chooseButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(USER_DIR);
				int state = chooser.showDialog(null, "Select");
				mdlnFile = chooser.getSelectedFile();
				compileButton.setEnabled(true);

				if(mdlnFile != null && state == JFileChooser.APPROVE_OPTION){
					System.out.println("File selected for compiling!");
					compileButton.setEnabled(true);
				}
				else if(mdlnFile == null && state == JFileChooser.CANCEL_OPTION){
					compileButton.setEnabled(false);
				}

			}

		});

		compileButton = new JButton("Compile");
		compileButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				chooseButton.setEnabled(false);
				// take the chosen file and parse/compile it
				if(compiler.compile(mdlnFile)){
					//send message to observers
					currProgram = compiler.getCurrProgram();
//					currProgram = (MDLnProgram) compiler.getCurrProgram();
					for(PanelObservable po : observers){
						po.notifyChange(new ObserverPacket(ObserverPacket.MessageType.NEW_PROGRAM, currProgram));
					}
				}
				
			}

		});
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				compiler.clearProgram();
				chooseButton.setEnabled(true);
				compileButton.setEnabled(false);
				for(PanelObservable po : observers){
					po.notifyChange(new ObserverPacket(ObserverPacket.MessageType.CANCELLED, currProgram));
				}
			}
			
			
		});


	}

	@Override
	protected final void buildLayout() {
		
		this.setBorder(BorderFactory.createTitledBorder("MDL Program Select"));
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		chooseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		compileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(chooseButton);
		this.add(compileButton);
		this.add(cancelButton);

	}

	public void notifyChange(ObserverPacket message) {
		// TODO Auto-generated method stub

	}

}
