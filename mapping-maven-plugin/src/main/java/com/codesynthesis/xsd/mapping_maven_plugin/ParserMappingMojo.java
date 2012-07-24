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

import java.util.ArrayList;

/**
 * C++/Tree is a W3C XML Schema to C++ mapping that represents the data stored in XML as a statically-typed, vocabulary-specific object model.
 *
 * ExecutionDescription="xsd cxx-parser [AllOptions] [AdditionalOptions] [Inputs]"
 *
 * @goal cxx-parser
 * @phase process-sources
 * @requiresProject true
 * @requiresDependencyResolution compile
 */
public class ParserMappingMojo
    extends AbstractCXXMappingMojo
{
	/**
	 * Generate validation code which ensures that instance documents conform to the schema. This is the default for the Expat XML parser.
	 * 
	 * @parameter expression="${mapping.genValidation}" default-value="false"
	 */
	private boolean genValidation;

	/**
	 * Suppress generation of validation code. This is the default for the Xerces-C++ XML parser.
	 * 
	 * @parameter expression="${mapping.supValidation}" default-value="false"
	 */
	private boolean supValidation;
	
	/**
	 * Use the provided suffix to construct the names of generated parser skeleton file.
	 * 
	 * @parameter expression="${mapping.skelFileSuffix}" default-value="-pskel"
	 */
	private String skelFileSuffix;
	
	/**
	 * Use the provided suffix to construct the names of generated parser skeletons.
	 * 
	 * @parameter expression="${mapping.skelTypeSuffix}" default-value="_pskel"
	 */
	private String skelTypeSuffix;
	
	/**
	 * Use the specified parser as the underlying XML parser.
	 * 
	 * Future enum
	 * xerces - (default) 0
	 * expat - expat,1
	 * 
	 * @parameter expression="${mapping.xmlParser}" default-value=""
	 */
//	private XMLParser xmlParser;
	private String xmlParser;
	
    
    @Override
    protected String mappingType() {
		return "cxx-parser";
	}

    @Override
    protected String[] getOptions() {
		ArrayList<String> result = new ArrayList<String>();
		
		if(genValidation)
			result.add("--generate-validation");
		if(supValidation)
			result.add("--suppress-validation");

		result.add("--skel-file-suffix");
		result.add(skelFileSuffix);

		result.add("--skel-type-suffix");
		result.add(skelTypeSuffix);

		if( xmlParser != null && ! ( xmlParser.isEmpty() || "0".equals(xmlParser) ) ) {
			result.add( "--xml-parser" );
			result.add( xmlParser );
		}
		// TODO: enum
//		if( XMLParser.valueOf(xmlParser).ordinal() == 1 ) {
//			result.add( "--xml-parser" );
//			result.add( "expat" );
//		}		
		return result.toArray(new String[0]);
    }
}
