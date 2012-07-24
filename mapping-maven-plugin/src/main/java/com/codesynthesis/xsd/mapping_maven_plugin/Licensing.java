package com.codesynthesis.xsd.mapping_maven_plugin;

/**
 * Licensing Proprietary License should contact  http://www.codesynthesis.com/contact/
 * 
 * @author gdomjan
 *
 */
public class Licensing {

	/**
	 * Indicate that the generated code is licensed under a proprietary license instead of the GPL.
	 * 
	 * @parameter expression="false"
	 */
	private boolean proprietaryLicense;
	
	/**
	 * The maximum number of lines of generated code we can produce under our current license.
	 * Default value is 10,000 for "Free Proprietary License for Small Vocabularies"
	 * 
	 * @parameter expression="10000"
	 */
	private String maxLineCount;

	public boolean isProprietaryLicense() {
		return proprietaryLicense;
	}

	public void setProprietaryLicense(boolean proprietaryLicense) {
		this.proprietaryLicense = proprietaryLicense;
	}

	public String getMaxLineCount() {
		return maxLineCount;
	}

	public void setMaxLineCount(String maxLineCount) {
		this.maxLineCount = maxLineCount;
	}

}
