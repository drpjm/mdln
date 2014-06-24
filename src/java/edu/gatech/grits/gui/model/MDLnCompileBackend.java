package edu.gatech.grits.gui.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;

import edu.gatech.grits.mdln.ASTDisplay;
import edu.gatech.grits.mdln.lang.MDLn;
import edu.gatech.grits.mdln.lang.util.MDLnProgram;
import edu.gatech.grits.mdln.lexer.Lexer;
import edu.gatech.grits.mdln.lexer.LexerException;
import edu.gatech.grits.mdln.node.Start;
import edu.gatech.grits.mdln.parser.Parser;
import edu.gatech.grits.mdln.parser.ParserException;

public class MDLnCompileBackend extends CompilerBackend {

	private boolean isProgramReady;

	/**
	 * This class provides the compiler backend for gui interaction. 
	 */
	public MDLnCompileBackend() {
		isProgramReady = false;
		currProgram = new MDLnProgram();
	}

	public boolean compile(File inputFile) {
		boolean success = false;
		
		FileReader inFile;
		try {
			inFile = new FileReader(inputFile);
			StringWriter test = new StringWriter();
			while (inFile.ready()) {
				test.write(inFile.read());
			}

			Parser p = new Parser(new Lexer(new PushbackReader(new StringReader(test.toString()))));

			Start s = p.parse();
			System.out.println("*** Compiling file... ***");
			MDLn mdlnCompiler=  new MDLn();
			s.apply(mdlnCompiler);
			// REMOVED temporarily!
//			mdlnCompiler.compile();
			
			//load properties needed for program distribution
			currProgram = mdlnCompiler.getMdlnProgram();
			
//			((MDLnProgram) currProgram).setAgentIds(mdlnCompiler.getAgentIds());
//			((MDLnProgram) currProgram).setAgentModes(mdlnCompiler.getAgentModeMap());
//			((MDLnProgram) currProgram).setAgentRoles(mdlnCompiler.getAgentRoles());
			
			
			success = true;
			this.isProgramReady = true;

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (ParserException e3) {
			e3.printStackTrace();
		} catch (LexerException e4) {
			e4.printStackTrace();
		}
		
		return success;

	}

	public boolean isProgramReady() {
		return this.isProgramReady;
	}

	public void clearProgram() {
		this.currProgram = null;
		this.isProgramReady = false;
	}

}
