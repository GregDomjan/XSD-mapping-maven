package com.codesynthesis.xsd.mapping_maven_plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SingleTargetSourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * 
 * @requiresProject true
 * @requiresDependencyResolution compile
 * @author gdomjan
 */
public abstract class AbstractCXXMappingMojo extends AbstractMappingMojo {

	/**
	 * The class directory to scan for class files with native interfaces.
	 * 
	 * @parameter default-value="${basedir}/src/main/xsd"
	 * @required
	 */
	protected File xsdInputDirectory;

	/**
	 * The class directory to scan for class files with native interfaces.
	 * 
	 * @parameter default-value="${basedir}/src/main/xsdconfig/xsd.config"
	 * @required
	 */
	protected File optionsFile;

	/**
	 * The granularity in milliseconds of the last modification date for testing
	 * whether a source needs re-compilation
	 * 
	 * @parameter default-value="0"
	 * @required
	 */
	protected int xsdStaleMillis = 0;
	/**
	 * Set this value if you wish to have a single timestamp file to track changes rather than cxx,hxx comparison
	 * The time-stamp file for the processed xsd files.
	 * 
	 * @parameter
	 */
	protected String xsdTimestampFile = null;
	/**
	 * The directory to store the time-stamp file for the processed aid files.
	 * Defaults to outputDirectory.  Only used with xsdTimestampFile being set.
	 * 
	 * @parameter default-value="${project.build.directory}/mapping/cpp"
	 */
	protected File xsdTimestampDirectory;
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
	 * Indicate proprietary license and line count limit.  If absent GPL is assumed with no line limit.
	 * 
	 * @parameter 
	 */
	protected Licensing licensing;
	
	/**
	 * Generate simple functions inline. This option triggers creation of the inline file.
	 * 
	 * @parameter expression="${mapping.genInline}" default-value="false"
	 */
	private boolean genInline;

	/**
	 * Generate polymorphism-aware code. Specify this option if you use substitution groups or xsi:type.
	 * 
	 * @parameter expression="${mapping.genPolymorphic}" default-value="false"
	 */
	private boolean genPolymorphic;

	/**
	 * Map XML Schema namespaces to C++ namespaces by specifying a comma-separated list of mapping rules in the form &lt;xml-ns&gt;=&lt;cxx-ns&gt; where &lt;xml-ns&gt; stands for an XML Schema namespace and &lt;cxx-ns&gt; - for a C++ namespace.
	 * 
	 * @parameter expression="${mapping.namespaceMap}" default-value=""
	 */
	// TODO: needs a->b, could make this structure.
	private String namespaceMap;

	/**
	 * Insert the specified symbol in places where DLL export/import control statements (__declspec(dllexport/dllimport)) are necessary.
	 * 
	 * @parameter expression="${mapping.exportSymbol}" default-value=""
	 */
	private String exportSymbol;

	/**
	 * Insert the contents of the file specified at the beginning of the header file.
	 * 
	 * @parameter expression="${mapping.headerPrologueFile}" default-value="${basedir}/src/main/xsdconfig/headerPrologue.txt"
	 */
	private File headerPrologueFile;

	/**
	 * Insert the contents of the file specified at the end of the header file.
	 * 
	 * @parameter expression="${mapping.headerEpilogueFile}" default-value="${basedir}/src/main/xsdconfig/headerEpilogue.txt"
	 */
	private File headerEpilogueFile;

	/**
	 * Character type to be used in the generated code.
	 * char, 0
	 * wchar, 1
	 * 
	 * @parameter expression="${mapping.charType}" default-value="char"
	 */
//	private CharType charType;  //maven3?
	private String charType;

	/**
	 * Character type to be used in the generated code.
	 * optional string   iso8859-1, lcp, custom
	 * 
	 * In future plan to make this fixed set
	 * UTF-8 (char)/UTF-16 (wchar_t) - (default) 0
	 * ISO-8859-1 - ISO8859_1, 1
	 * Xerces-C++ local code page - LOCAL_CODEPAGE, 2
	 * Custom encoding - CUSTOM, 3
	 * 
	 * @parameter expression="${mapping.charType}" default-value=""
	 */
//	private CharEncoding charEncoding;  //maven3?
	private String charEncoding;

	/**
	 * Properties catch all in case we missed some configuration.
	 *
	 * @parameter
	 */
	private Properties otherProperties;
	
	/**
	 * Leave the xsd-tools behind after compilation for extended use outside this goal.
	 *
	 * @parameter expression="${mapping.extendedUse}" default-value="false"
	 */
	protected boolean extendedUse;
	
	/** 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly 
	 * */
	protected MavenProject mavenProject;
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
    * The Zip archiver.
    * @component role="org.codehaus.plexus.archiver.UnArchiver" roleHint="zip"
    * @readonly
    * @required
    */
    private ZipUnArchiver zipUnArchiver;
    
	protected final String toolsId = "mapping-tools";

	public AbstractCXXMappingMojo() {
		super();
	}

	public final Set<String> getIncludes() {
	    if ( includes.isEmpty() )
	    {
	        includes.add( "**/*.xsd" );
	    }
	    return includes;
	}

	public final Set<String> getExcludes() {
	    return excludes;
	}

	protected void unpackFileBasedResources(File toolDirectory)	throws MojoExecutionException {
		getLog().info("unpacking xsd binaries");

	    Artifact artifact = findToolsArtifact();
        
	    getLog().info("Using mapping-tools " + artifact );
	    
	    File pluginJar = artifact.getFile();

    	getLog().info("Extracting " + pluginJar + " to " + toolDirectory );

    	zipUnArchiver.setSourceFile(pluginJar);
    	// bug: having trouble with extract method of unarchiver - nothing being extracted, or crashing on log file when creating the archiver directly.
//    	zipUnArchiver.setDestDirectory( toolDirectory );
//    	zipUnArchiver.setOverwrite(true);
//    	zipUnArchiver.extract();
        zipUnArchiver.extract( "bin", toolDirectory );
// TODO: Still deciding if this content should be optional    	        
//        if( visualStudioUse )
        	zipUnArchiver.extract( "etc", toolDirectory );

	    
	    if( !toolDirectory.exists() )
	    	throw new MojoExecutionException( "Error extracting resources from mapping-tools.");
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

	protected void cleanupFileBasedResources(File toolDirectory) {
		try {
			FileUtils.deleteDirectory(toolDirectory);
		} catch (IOException e) {
			getLog().warn("Post build cleanup - Unable to cleanup mapping-tools folder", e);
		}
	}

	protected void updateProject() {
        getLog().debug( "Adding " + outputDirectory.getAbsolutePath() + " to the project's compile sources." );
    	mavenProject.addCompileSourceRoot( outputDirectory.getAbsolutePath() );

//        Resource resource = new Resource();
//        resource.setDirectory(compilerParams.getClassesDir().getAbsolutePath());
//        resource.setFiltering(false);
//        mavenProject.addResource(resource);
    }

	protected void addLicensing(Commandline cl) {
		if( licensing != null )
        {
        	if( licensing.isProprietaryLicense() )
	        	cl.addArguments( new String[] {
	    	        	"--proprietary-license"
	            	});
        	
        	if( ! licensing.isProprietaryLicense() && Integer.parseInt(licensing.getMaxLineCount()) > 10000 )
        		getLog().warn("Configured licensing is not valid");
        	
        	cl.addArguments( new String[] {
				"--sloc-limit"
				, licensing.getMaxLineCount()
        	});
        }
	}
	
	protected void addOptions(Commandline cl) {
		if( optionsFile!= null && optionsFile.exists() )
			cl.addArguments( new String[] { "--options-file" , optionsFile.getAbsolutePath() } );
		
	    cl.addArguments( new String[] { "--output-dir" , outputDirectory.getAbsolutePath() } );

		if( genInline )
			cl.addArguments( new String[] { "--generate-inline" } );
		if( genPolymorphic )
			cl.addArguments( new String[] { "--generate-polymorphic" } );

		cl.addArguments( new String[] { "--hxx-suffix", headerSuffix } );
		cl.addArguments( new String[] { "--cxx-suffix", sourceSuffix } );
		
		if( genInline )
			cl.addArguments( new String[] { "--ixx-suffix", inlineSuffix } );

		if( namespaceMap != null && !namespaceMap.trim().isEmpty() )
			cl.addArguments( new String[] { "--namespace-map", namespaceMap.trim() } );
	
		if( exportSymbol != null && !exportSymbol.trim().isEmpty() )
			cl.addArguments( new String[] { "--export-symbol", exportSymbol.trim() } );
		
		//TODO: not sure this is the best form of the path to use.
		if( headerPrologueFile != null && headerPrologueFile.exists() )
			cl.addArguments( new String[] { "--hxx-prologue-file", headerPrologueFile.toString() } );

		if( headerEpilogueFile != null && headerEpilogueFile.exists() )
			cl.addArguments( new String[] { "--hxx-epilogue-file", headerEpilogueFile.toString() } );	
		
		if( CharType.valueOf(charType.toUpperCase()).ordinal() == 1 )
			cl.addArguments( new String[] { "--char-type", "wchar_t" } );

		if( charEncoding!=null && !charEncoding.isEmpty() )
			cl.addArguments( new String[] { "--char-encoding", charEncoding } );
// TODO: checked char encoding
//		switch (CharType.valueOf(charEncoding).ordinal()) {
//		case 0:
//			break;
//
//		case 1:
//			cl.addArguments( new String[] { "--char-encoding", "iso8859-1" } );
//			break;
//
//		case 2:
//			cl.addArguments( new String[] { "--char-encoding", "lcp" } );
//			break;
//
//		case 3:
//			cl.addArguments( new String[] { "--char-encoding", "custom" } );
//			break;
//
//		default:
//			break;
//		}

	}

	protected void addOtherOptions(Commandline cl) {
		if ( otherProperties!= null && !otherProperties.isEmpty()) {
			ArrayList<String> result = new ArrayList<String>();

			for (Enumeration<Object> keys = otherProperties.keys(); keys.hasMoreElements();) {
				String key = (String)keys.nextElement();
				result.add(key);
				String value = otherProperties.getProperty(key);
				if( null != value )
				{
					result.add(value);
				}
			}

			cl.addArguments(result.toArray(new String[0]));
		}
	}
	
	protected void generateTree( Set<String> files, File toolDirectory)
			throws MojoExecutionException {
		
	    Commandline cl = new Commandline();
	    cl.setExecutable( toolDirectory.getAbsolutePath() + "/bin/xsd");
	    cl.addArguments(new String[]{ mappingType()} );	

	    addLicensing(cl);
	    addOptions(cl);
	    cl.addArguments(getOptions());
	    addOtherOptions(cl);
	
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
	
	        // TODO: maybe should report or do something with return value.
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

	public void execute() throws MojoExecutionException {
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
		        	Set<String> fileExts = new HashSet<String>();
		        	fileExts.add( sourceSuffix );
		        	fileExts.add( headerSuffix );
	// TODO: what should we check for stale? Do we need to check all?
	//	        	if( genInline ){
	//	        		fileExts.add( inlineSuffix );
	//	        	}
		        	xsdTimestampDirectory = outputDirectory;
		            scanner.addSourceMapping( new SuffixMapping( ".xsd", fileExts ) );
		        }
		
		        Set<File> schemas = scanner.getIncludedSources( xsdInputDirectory, xsdTimestampDirectory );
				File toolDirectory = new File( mavenProject.getBuild().getDirectory(), toolsId );
	
	    		if( extendedUse )
	    			unpackFileBasedResources(toolDirectory);
	
		        if ( !schemas.isEmpty() )
		        {
		            Set<String> files = new HashSet<String>();
		            for ( Iterator<File> i = schemas.iterator(); i.hasNext(); )
		            {
	                    files.add( i.next().getPath() );
		            }
		            
		            if ( !files.isEmpty() )
		            {
		        		if( ! extendedUse )
		        			unpackFileBasedResources(toolDirectory);
	
		        	    generateTree( files, toolDirectory );
		        	    
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
		        
	    		if( ! extendedUse )
	    			cleanupFileBasedResources(toolDirectory);
	
		        updateProject( );
		    }
		    catch ( InclusionScanException e )
		    {
		        throw new MojoExecutionException( "XSD: scanning for updated files failed", e );
		    }
		    
	    }

	abstract String mappingType();
	abstract String[] getOptions();
}