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
 * ExecutionDescription="xsd cxx-tree [AllOptions] [AdditionalOptions] [Inputs]"
 *
 * @goal cxx-tree
 * @phase process-sources
 * @requiresProject true
 * @requiresDependencyResolution compile
 */
public class TreeMappingMojo
    extends AbstractCXXMappingMojo
{
	/**
	 * Generate a separate header file with forward declarations for the types being generated.
	 * 
	 * @parameter expression="${mapping.genForward}" default-value="false"
	 */
	private boolean genForward;
	
	/**
	 * Generated C++ forward declaration file suffix
	 * 
	 * @parameter expression="${mapping.forwardSuffix}" default-value="-fwd.hxx"
	 */
	private String forwardSuffix;

	/**
	 * Generate serialization functions. Serialization functions convert the object model back to XML.
	 * 
	 * @parameter expression="${mapping.genSerialization}" default-value="false"
	 */
	private boolean genSerialization;

	/**
	 * Suppress generation of the parsing functions and constructors.
	 * 
	 * @parameter expression="${mapping.supParsing}" default-value="false"
	 */
	private boolean supParsing;

	/**
	 * Generate polymorphism-aware code. Specify this option if you use substitution groups or xsi:type.
	 * 
	 * @parameter expression="${mapping.genPolymorphic}" default-value="false"
	 */
	private boolean genPolymorphic;

	/**
	 * Generate ostream insertion operators for generated types. This allows to easily print a fragment or the whole object model for debugging or logging.
	 * 
	 * @parameter expression="${mapping.genOstream}" default-value="false"
	 */
	private boolean genOstream;

	/**
	 * Generate documentation comments suitable for extraction by the Doxygen documentation system.
	 * 
	 * @parameter expression="${mapping.genDoxygen}" default-value="false"
	 */
	private boolean genDoxygen;
	
	/**
	 * Generate comparison operators for complex types.
	 * 
	 * @parameter expression="${mapping.genComparison}" default-value="false"
	 */
	private boolean genComparison;

	/**
	 * Generate default constructors even for types that have required members.
	 * 
	 * @parameter expression="${mapping.genDefaultCtor}" default-value="false"
	 */
	private boolean genDefaultCtor;

	/**
	 * Generate constructors that expect an instance of a base type followed by all required members.
	 * 
	 * @parameter expression="${mapping.genFromBaseCtor}" default-value="false"
	 */
	private boolean genFromBaseCtor;

	/**
	 * Generate accessors and modifiers as well as parsing and serialization code for XML Schema wildcards (any and anyAttribute).
	 * 
	 * @parameter expression="${mapping.genWildcard}" default-value="false"
	 */
	private boolean genWildcard;
	
	/**
	 * Generate workarounds for IntelliSense bugs in Visual Studio 2005 (8.0).
	 * 
	 * @parameter expression="${mapping.genIntellisense}" default-value="false"
	 */
	private boolean genIntellisense;

	/**
	 * Specify the type naming convention that should be used in the generated code.
	 * unset for default (K&R)
	 * java, ucc
	 * 
	 * @parameter expression="${mapping.typeNaming}" default-value=""
	 */
	//private TypeNaming typeNaming;
	private String typeNaming;
	
	/**
	 * Specify the function naming convention that should be used in the generated code.
	 * unset for default (K&R)
	 * java, lcc
	 * 
	 * @parameter expression="${mapping.functionNaming}" default-value=""
	 */
	//private FunctionNaming functionNaming;
	private String functionNaming;
	
	/**
	 * Treat only specified comma-separated list of elements as document roots.
	 * 
	 * @parameter expression="${mapping.rootElements}" default-value=""
	 */
	private String rootElements;
	
    
    @Override
    protected String mappingType() {
		return "cxx-tree";
	}

    @Override
    protected String[] getOptions() {
		ArrayList<String> result = new ArrayList<String>();

		if( genForward ){
			result.add("--generate-forward");
			result.add("--fwd-suffix");
			result.add(forwardSuffix);
		}

		if(genSerialization)
			result.add("--generate-serialization");
		if(supParsing)
			result.add("--suppress-parsing");
		if(genPolymorphic)
			result.add("--generate-polymorphic");
		if(genOstream)
			result.add("--generate-ostream");
		if(genDoxygen)
			result.add("--generate-doxygen");
		if(genComparison)
			result.add("--generate-comparison");
		if(genDefaultCtor)
			result.add("--generate-default-ctor");
		if(genFromBaseCtor)
			result.add("--generate-from-base-ctor");
		if(genWildcard)
			result.add("--generate-wildcard");
		if(genIntellisense)
			result.add("--generate-intellisense");

		if( rootElements != null && !rootElements.trim().isEmpty() ){
			String[] roots = rootElements.split(",");
			for( String root : roots ){
				result.add("--root-element");
				result.add(root.trim());
			}
		}

		if( typeNaming != null && ! ( typeNaming.isEmpty() || "0".equals(typeNaming) ) ) {
			result.add( "--type-naming" );
			result.add( typeNaming );
		}
		
		if( functionNaming != null && ! ( functionNaming.isEmpty() || "0".equals(functionNaming) ) ) {
			result.add( "--function-naming" );
			result.add( functionNaming );
		}
		
		return result.toArray(new String[0]);
	}

}
