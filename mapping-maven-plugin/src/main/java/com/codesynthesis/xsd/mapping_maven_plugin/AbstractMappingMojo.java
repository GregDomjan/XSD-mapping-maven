package com.codesynthesis.xsd.mapping_maven_plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;

public abstract class AbstractMappingMojo extends AbstractMojo {

	/**
	 * Output directory for the generated files
	 * 
	 * @parameter expression="${project.build.directory}/mapping/cpp" default-value="${project.build.directory}/mapping/cpp"
	 * @required
	 */
	protected File outputDirectory;
	/**
	 * See <a href="http://java.sun.com/javase/6/docs/technotes/tools/windows/jarsigner.html#Options">options</a>.
	 * 
	 * @parameter expression="${mapping.verbose}" default-value="false"
	 */
	protected boolean verbose;
	/**
	 * Generated C++ header file suffix
	 * 
	 * @parameter expression="${mapping.hxx.suffix}" default-value=".hxx"
	 */
	protected String headerSuffix;
	/**
	 * Generated C++ source file suffix
	 * 
	 * @parameter expression="${mapping.cxx.suffix}" default-value=".cxx"
	 */
	protected String sourceSuffix;
	/**
	 * Generated C++ inline file suffix
	 * 
	 * @parameter expression="${mapping.ixx.suffix}" default-value=".ixx"
	 */
	protected String inlineSuffix;

	public AbstractMappingMojo() {
		super();
	}

}