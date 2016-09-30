package ca.concordia.cssanalyser.plugin.annotations;

public enum CSSAnnotationType {
	
	DUPLICATION("ca.concordia.cssanalyser.plugin.annotations.DuplicatedDeclarations"), 
	STYLE_RULE("ca.concordia.cssanalyser.plugin.annotations.StyleRule");
	
	private String value;
		
	private CSSAnnotationType(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}

}
