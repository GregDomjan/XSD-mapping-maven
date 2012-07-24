/**
 * 
 */
package com.codesynthesis.xsd.mapping_maven_plugin;

/**
 * @author gdomjan
 * 
 */
public enum CharType {

    CHAR("char"),
    WCHAR("wchar");

    private CharType(String key){
        this.key = key;
    }

    private String key;

    public String getKey(){
        return this.key;
    }

//    static CharType valueOf( String key ){
//    	return 
//    }
}
