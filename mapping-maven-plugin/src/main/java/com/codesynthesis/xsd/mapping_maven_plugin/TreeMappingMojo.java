package com.codesynthesis.xsd.mapping_maven_plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SingleTargetSourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.component.configurator.converters.composite.ArrayConverter;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * C++/Tree is a W3C XML Schema to C++ mapping that represents the data stored in XML as a statically-typed, vocabulary-specific object model.
 *
 * @goal cxx-tree
 * @phase process-sources
 * @requiresProject true
 * @requiresDependencyResolution compile
 */
public class TreeMappingMojo
    extends AbstractMojo
{
	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}/mapping/cpp"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * The class directory to scan for class files with native interfaces.
	 * 
	 * @parameter default-value="${basedir}/src/main/xsd"
	 * @required
	 */
	private File xsdInputDirectory;

	/**
	 * The granularity in milliseconds of the last modification date for testing
	 * whether a source needs re-compilation
	 * 
	 * @parameter default-value="0"
	 * @required
	 */
	private int xsdStaleMillis = 0;

	/**
	 * Set this value if you wish to have a single timestamp file to track changes rather than cxx,hxx comparison
	 * The time-stamp file for the processed xsd files.
	 * 
	 * @parameter
	 */
	private String xsdTimestampFile = null;

	/**
	 * The directory to store the time-stamp file for the processed aid files.
	 * Defaults to outputDirectory.  Only used with xsdTimestampFile being set.
	 * 
	 * @parameter default-value="${project.build.directory}/mapping/cpp"
	 */
	private File xsdTimestampDirectory;


    /**
     * The set of files/patterns to include Defaults to "**\/*.class"
     * 
     * @parameter
     */
    private Set<String> includes = new HashSet<String>();

    /**
     * A list of exclusion filters.
     * 
     * @parameter
     */
    private Set<String> excludes = new HashSet<String>();

	/**
	 * See <a href="http://java.sun.com/javase/6/docs/technotes/tools/windows/jarsigner.html#Options">options</a>.
	 * 
	 * @parameter expression="${jarsigner.verbose}" default-value="false"
	 */
    private boolean verbose;
    
//    
//	<CustomBuildRule
//	Name="C++/Tree Mapping Rule"
//	DisplayName="C++/Tree Mapping Rule"
//	CommandLine="$(SDKHOME)\xsd\xsd-3.2.0-i686-windows\bin\xsd.exe cxx-tree [AllOptions] [AdditionalOptions] [Inputs]"
//	Outputs="[$XSDOutDir]\$(InputName)[$HeaderSuffix];[$XSDOutDir]\$(InputName)[$SourceSuffix]"
//	FileExtensions="*.xsd"
//	ExecutionDescription="xsd cxx-tree [AllOptions] [AdditionalOptions] [Inputs]"
//	ShowOnlyRuleProperties="false"
//	>
//	<Properties>
//		<StringProperty
//			Name="XSDOutDir"
//			DisplayName="Output Directory"
//			Category="Output"
//			Description="Output directory for the generated files"
//			Switch="--output-dir [value]"
//			DefaultValue="."
//		/>
//		<BooleanProperty
//			Name="ProprietaryLicense"
//			IsReadOnly="true"
//			DisplayName="Proprietary License"
//			Category="Licensing"
//			Description="Indicate that the generated code is licensed under a proprietary license instead of the GPL."
//			Switch="--proprietary-license"
//			DefaultValue="true"
//		/>
//		<StringProperty
//			Name="HeaderSuffix"
//			DisplayName="C++ Header Suffix "
//			Category="Output"
//			Description="Generated C++ header file suffix"
//			Switch="--hxx-suffix [value]"
//			DefaultValue=".hxx"
//		/>
//		<StringProperty
//			Name="SourceSuffix"
//			DisplayName="C++ Source Suffix"
//			Category="Output"
//			Description="Generated C++ source file suffix"
//			Switch="--cxx-suffix [value]"
//			DefaultValue=".cxx"
//		/>
//		<StringProperty
//			Name="InlineSuffix"
//			DisplayName="C++ Inline Suffix"
//			Category="Output"
//			Description="Generated C++ inline file suffix"
//			Switch="--ixx-suffix [value]"
//			DefaultValue=".ixx"
//		/>
//		<StringProperty
//			Name="ForwardSuffix"
//			DisplayName="C++ Forward Declaration Suffix"
//			Category="Output"
//			Description="Generated C++ forward declaration file suffix"
//			Switch="--fwd-suffix [value]"
//			DefaultValue="-fwd.hxx"
//		/>
//		<EnumProperty
//			Name="CharType"
//			DisplayName="Char Type"
//			PropertyPageName="Code Generation"
//			Description="Character type to be used in the generated code."
//			>
//			<Values>
//				<EnumValue
//					Value="0"
//					DisplayName="char"
//				/>
//				<EnumValue
//					Value="1"
//					Switch="--char-type wchar_t"
//					DisplayName="wchar_t"
//				/>
//			</Values>
//		</EnumProperty>
//		<StringProperty
//			Name="NamespaceMap"
//			DisplayName="Namespace Map"
//			PropertyPageName="Code Generation"
//			Description="Map XML Schema namespaces to C++ namespaces by specifying a comma-separated list of mapping rules in the form &lt;xml-ns&gt;=&lt;cxx-ns&gt; where &lt;xml-ns&gt; stands for an XML Schema namespace and &lt;cxx-ns&gt; - for a C++ namespace."
//			Switch="--namespace-map [value]"
//			Delimited="true"
//			Delimiters=","
//		/>
//		<BooleanProperty
//			Name="GenInline"
//			DisplayName="Generate Inline"
//			PropertyPageName="Code Generation"
//			Description="Generate simple functions inline. This option triggers creation of the inline file."
//			Switch="--generate-inline"
//		/>
//		<BooleanProperty
//			Name="GenForward"
//			DisplayName="Generate Forward"
//			PropertyPageName="Code Generation"
//			Description="Generate a separate header file with forward declarations for the types being generated."
//			Switch="--generate-forward"
//		/>
//		<BooleanProperty
//			Name="GenSerialization"
//			DisplayName="Generate Serialization"
//			PropertyPageName="Code Generation"
//			Description="Generate serialization functions. Serialization functions convert the object model back to XML."
//			Switch="--generate-serialization"
//		/>
//		<BooleanProperty
//			Name="SupParsing"
//			DisplayName="Suppress Parsing"
//			PropertyPageName="Code Generation"
//			Description="Suppress generation of the parsing functions and constructors."
//			Switch="--suppress-parsing"
//		/>
//		<BooleanProperty
//			Name="GenPolymorphic"
//			DisplayName="Generate Polymorphic"
//			PropertyPageName="Code Generation"
//			Description="Generate polymorphism-aware code. Specify this option if you use substitution groups or xsi:type."
//			Switch="--generate-polymorphic"
//		/>
//		<BooleanProperty
//			Name="GenOstream"
//			DisplayName="Generate Ostream"
//			PropertyPageName="Code Generation"
//			Description="Generate ostream insertion operators for generated types. This allows to easily print a fragment or the whole object model for debugging or logging."
//			Switch="--generate-ostream"
//		/>
//		<BooleanProperty
//			Name="GenDoxygen"
//			DisplayName="Generate Doxygen"
//			PropertyPageName="Code Generation"
//			Description="Generate documentation comments suitable for extraction by the Doxygen documentation system."
//			Switch="--generate-doxygen"
//		/>
//		<BooleanProperty
//			Name="GenComparison"
//			DisplayName="Generate Comparison"
//			PropertyPageName="Code Generation"
//			Description="Generate comparison operators for complex types."
//			Switch="--generate-comparison"
//		/>
//		<BooleanProperty
//			Name="GenDefaultCtor"
//			DisplayName="Generate Default Constructors"
//			PropertyPageName="Code Generation"
//			Description="Generate default constructors even for types that have required members."
//			Switch="--generate-default-ctor"
//		/>
//		<BooleanProperty
//			Name="GenFromBaseCtor"
//			DisplayName="Generate From-Base Constructors"
//			PropertyPageName="Code Generation"
//			Description="Generate constructors that expect an instance of a base type followed by all required members."
//			Switch="--generate-from-base-ctor"
//		/>
//		<BooleanProperty
//			Name="GenWildcard"
//			DisplayName="Generate Wildcard"
//			PropertyPageName="Code Generation"
//			Description="Generate accessors and modifiers as well as parsing and serialization code for XML Schema wildcards (any and anyAttribute)."
//			Switch="--generate-wildcard"
//		/>
//		<BooleanProperty
//			Name="GenIntellisense"
//			DisplayName="Generate IntelliSense"
//			PropertyPageName="Code Generation"
//			Description="Generate workarounds for IntelliSense bugs in Visual Studio 2005 (8.0)."
//			Switch="--generate-intellisense"
//			DefaultValue="true"
//		/>
//		<EnumProperty
//			Name="TypeNaming"
//			DisplayName="Type Naming"
//			PropertyPageName="Code Generation"
//			Description="Specify the type naming convention that should be used in the generated code."
//			>
//			<Values>
//				<EnumValue
//					Value="0"
//					DisplayName="K&amp;R"
//				/>
//				<EnumValue
//					Value="1"
//					Switch="--type-naming java"
//					DisplayName="Java"
//				/>
//				<EnumValue
//					Value="2"
//					Switch="--type-naming ucc"
//					DisplayName="Upper Camel Case"
//				/>
//			</Values>
//		</EnumProperty>
//		<EnumProperty
//			Name="FunctionNaming"
//			DisplayName="Function Naming"
//			PropertyPageName="Code Generation"
//			Description="Specify the function naming convention that should be used in the generated code."
//			>
//			<Values>
//				<EnumValue
//					Value="0"
//					DisplayName="K&amp;R"
//				/>
//				<EnumValue
//					Value="1"
//					Switch="--function-naming java"
//					DisplayName="Java"
//				/>
//				<EnumValue
//					Value="2"
//					Switch="--function-naming lcc"
//					DisplayName="Lower Camel Case"
//				/>
//			</Values>
//		</EnumProperty>
//		<StringProperty
//			Name="RootElement"
//			DisplayName="Root Element"
//			PropertyPageName="Code Generation"
//			Description="Treat only specified comma-separated list of elements as document roots."
//			Switch="--root-element [value]"
//			Delimited="true"
//			Delimiters=","
//		/>
//		<StringProperty
//			Name="ExportSymbol"
//			DisplayName="Export Symbol"
//			PropertyPageName="Code Generation"
//			Description="Insert the specified symbol in places where DLL export/import control statements (__declspec(dllexport/dllimport)) are necessary."
//			Switch="--export-symbol [value]"
//		/>
//		<StringProperty
//			Name="HxxPrologue"
//			DisplayName="Header Prologue File"
//			PropertyPageName="Code Generation"
//			Description="Insert the contents of the file specified at the beginning of the header file."
//			Switch="--hxx-prologue-file [value]"
//		/>
//		<StringProperty
//			Name="HxxEpilogue"
//			DisplayName="Header Epilogue File"
//			PropertyPageName="Code Generation"
//			Description="Insert the contents of the file specified at the end of the header file."
//			Switch="--hxx-epilogue-file [value]"
//		/>
//		<IntegerProperty
//			Name="SlocLimit"
//			IsReadOnly="true"
//			DisplayName="Max line count"
//			Category="Licensing"
//			Description="The maximum number of lines of generated code we can produce under our current license."
//			Switch="--sloc-limit [value]"
//			DefaultValue="20000"
//		/>
//	</Properties>
//</CustomBuildRule>

    /** 
     * @parameter default-value="${project}"
     * @required
     * @readonly 
     * */
    private MavenProject mavenProject;

    /** 
     * @parameter default-value="${plugin.groupId}"
     * @required
     * @readonly 
     * */
    private String pluginGroupId;

    /** 
     * @parameter default-value="${plugin.artifacts}"
     * @required
     * @readonly 
     * */
    private List artifacts;
    
    /** 
     * @parameter default-value="${plugin.dependencies}"
     * @required
     * @readonly 
     * */
    private List dependencies;

    final String toolsId = "mapping-tools";
    
    public void execute()
        throws MojoExecutionException
    {
    	if( !xsdInputDirectory.exists() )
    	{
    		getLog().info("XSD: input directory not found, no classes to process for JNI generation");
    		return;
    	}
    	
	    try
	    {
            if( outputDirectory!= null )
            	outputDirectory.mkdirs();           
	    	
	        SourceInclusionScanner scanner = new StaleSourceScanner( xsdStaleMillis, getIncludes(), getExcludes() );
	        if ( xsdTimestampFile!= null && xsdTimestampDirectory!= null )
	        {
		    	if( !xsdTimestampDirectory.exists() )
		    		xsdTimestampDirectory.mkdirs();
		    	// if( !xsdTimestampDirectory.exists() || xsdTimestampFile.isEmpty() )  tracking isn't going to work, always rebuild - warning? 
		    		
	            scanner.addSourceMapping( new SingleTargetSourceMapping( ".xsd", xsdTimestampFile ) );
	        }
	        else
	        {
	        	xsdTimestampDirectory = outputDirectory;
	            scanner.addSourceMapping( new SuffixMapping( ".xsd", new HashSet<String>(Arrays.asList(".cxx", ".hxx" ) ) ) );
	        }
	
	        Set<File> schemas = scanner.getIncludedSources( xsdInputDirectory, xsdTimestampDirectory );
	
	        if ( !schemas.isEmpty() )
	        {
	            Set<String> files = new HashSet<String>();
	            for ( Iterator<File> i = schemas.iterator(); i.hasNext(); )
	            {
                    files.add( i.next().getPath() );
	            }
	            
	            if ( !files.isEmpty() )
	            {
	        	    generateTree( files );
	        	    
	        	    if ( xsdTimestampFile!= null && xsdTimestampDirectory!= null ){
	        	    	File timeStamp = new File( xsdTimestampDirectory, xsdTimestampFile );
	        	    	if( ! timeStamp.exists() )
							try {
								timeStamp.createNewFile();
							} catch (IOException e) {
								getLog().warn("XSD: Unable to touch timestamp file");
							}
						else
	        	    		if( ! timeStamp.setLastModified(System.currentTimeMillis()) )
	        	    			getLog().warn("XSD: Unable to touch timestamp file");
	        	    }
	            }
	        }
	        
	        updateProject( );
	    }
	    catch ( InclusionScanException e )
	    {
	        throw new MojoExecutionException( "XSD: scanning for updated files failed", e );
	    }
	    
    }

    public final Set<String> getIncludes()
    {
        if ( includes.isEmpty() )
        {
            includes.add( "**/*.xsd" );
        }
        return includes;
    }
    
    public final Set<String> getExcludes()
    {
        return excludes;
    }
    

    protected void updateProject( )
    {
        getLog().debug( "Adding " + outputDirectory.getAbsolutePath() + " to the project's compile sources." );
    	mavenProject.addCompileSourceRoot( outputDirectory.getAbsolutePath() );

//        Resource resource = new Resource();
//        resource.setDirectory(compilerParams.getClassesDir().getAbsolutePath());
//        resource.setFiltering(false);
//        mavenProject.addResource(resource);
    }

	protected void generateTree( Set<String> files ) throws MojoExecutionException {
		File toolDirectory = new File( mavenProject.getBuild().getDirectory(), toolsId );
		
        unpackFileBasedResources(toolDirectory);
        
        Commandline cl = new Commandline( toolDirectory.getAbsolutePath() + "/bin/xsd");	// C:/data/dev/prj/sso/sdks\xsd\xsd-3.2.0-i686-windows\bin\xsd.exe
//         cxx-tree  --output-dir c:\Data\dev\prj\sso\SecureLogin-7-0-3-X\nsl\Wizard\model\ --proprietary-license --hxx-suffix .h --cxx-suffix .cpp --ixx-suffix .ixx --fwd-suffix -fwd.hxx --char-type wchar_t --generate-serialization --generate-ostream --generate-intellisense --type-naming ucc --function-naming lcc --root-element application --root-element selection --sloc-limit 20000  
        cl.addArguments( new String[] { 
        		"cxx-tree"
        		, "--output-dir"
        		, outputDirectory.getAbsolutePath() 
        		 } 
        );

//        cl.addArguments( new String[] { "--options-file" , outputDirectory.getAbsolutePath() } );

        cl.addArguments(files.toArray(new String[0]));
        
        try {
            if ( verbose )
            {
                getLog().info( cl.toString() );
            }
            else
            {
                getLog().debug( cl.toString() );
            }

            int returnValue = CommandLineUtils.executeCommandLine(cl, new StreamConsumer()
            {

                public void consumeLine( final String line )
                {
                    if ( verbose )
                    {
                        getLog().info( line );
                    }
                    else
                    {
                        getLog().debug( line );
                    }
                }

            }, new StreamConsumer()
            {

                public void consumeLine( final String line )
                {
                    getLog().warn( line );
                }

            } );
		} catch (CommandLineException e) {
			throw new MojoExecutionException( "Error running mapping-tools.", e );
		}
	}
    
    private void unpackFileBasedResources(File toolDirectory)
    	    throws MojoExecutionException
    	{
    		getLog().info("unpacking xsd binaries");
    	    if ( mavenProject == null || dependencies == null)
    	    {
    	        return;
    	    }
    	    
    	    // What we need to write out any resources in the plugin to the target directory of the
    	    // mavenProject using the Ant-based plugin:
    	    //
    	    // 1. Need a reference to the mapping-tools JAR itself
    	    // 2. Need a reference to the ${basedir} of the mavenProject

    	    Artifact artifact = findToolsArtifact();
            
    	    getLog().info("Using mapping-tools " + artifact );
    	    
    	    File pluginJar = artifact.getFile();

    	    try
    	    {
    	    	getLog().info("Extracting " + pluginJar + " to " + toolDirectory );
    	        UnArchiver ua = new ZipUnArchiver( pluginJar );

    	        ua.extract( "bin", toolDirectory );
    	    }
    	    catch ( ArchiverException e )
    	    {
    	        throw new MojoExecutionException( "Error extracting resources from mapping-tools.", e );
    	    }
    	}

	protected Artifact findToolsArtifact() throws MojoExecutionException {
		//return (Artifact) mavenPlugin.getArtifactMap().get(ArtifactUtils.versionlessKey(mavenPlugin.getGroupId(), toolsId));
		if( null != artifacts ) {
			for (Iterator artifactIterator = artifacts.iterator(); artifactIterator.hasNext();) {
			  Artifact artifact = (Artifact) artifactIterator.next();
			  if (artifact.getGroupId().equals(pluginGroupId) && artifact.getArtifactId().equals(toolsId)) {  // && artifact.getClassifier().equals( os. )
				return artifact;
			  }
			}
		}
		getLog().error( String.format("Tools Artifact %1$s:%2$s not found", pluginGroupId, toolsId ) );
		throw new MojoExecutionException("Unable to find mapping-tools dependency");
	}
}
