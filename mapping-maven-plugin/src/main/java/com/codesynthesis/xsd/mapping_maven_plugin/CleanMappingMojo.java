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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Cleanup generated source files in the outputDirectory
 *
 * @goal clean
 * @phase clean
 * @requiresProject true
 */
public class CleanMappingMojo
    extends AbstractMappingMojo
{
    
    public void execute()
        throws MojoExecutionException
    {
    	if( ! outputDirectory.exists() )
    		return;
    	
    	// TODO: Check if there is other content?
		try {
//			FileUtils.deleteDirectory(outputDirectory);
			List files = FileUtils.getFiles(outputDirectory, "*.hxx,*.ixx,*.cxx,*.h,*.hpp,*.cpp", "");//getIncludes(), getExcludes());
			for( Object fileObj : files){
				File file = (File)fileObj;
				file.delete();
			}
		} catch (IOException e) {
			getLog().warn("Post build cleanup - Unable to cleanup mapping-tools folder", e);
		}

    }

}
