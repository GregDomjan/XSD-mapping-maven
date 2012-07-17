XSD-mapping-maven
=================
(Initial structure, work in progress - tools don't get executed with all necessary config yet)

Mavenised Code Synthesis XSD mapping to C++ 
 - mapping-tools OS specific resources 
 - mapping-maven-plugin to execute the tools from maven project and generate source
 - NAR library

http://www.codesynthesis.com/projects/xsd/
CodeSynthesis XSD is a W3C XML Schema to C++ translator. It generates vocabulary-specific, statically-typed C++ mappings (also called bindings) from XML Schema definitions. XSD supports two C++ mappings: in-memory C++/Tree and event-driven C++/Parser.

The C++/Tree mapping consists of C++ classes that represent data types defined in XML Schema, a set of parsing functions that convert XML documents to a tree-like in-memory object model, and a set of serialization functions that convert the object model back to XML.

The C++/Parser mapping provides parser skeletons for data types defined in XML Schema. Using these parser skeletons you can build your own in-memory representations or perform immediate processing of XML documents.

License
-------
http://www.codesynthesis.com/licenses/gpl-2.txt
See http://www.codesynthesis.com/projects/xsd/ for details

This maven packaging captures and makes available these tools based on the following advice on that site where there are also other licences and exceptions.
 You can use, distribute, and/or modify XSD, its runtime library, and the generated code under the terms of the GNU General Public License, version 2 as published by the Free Software Foundation.

