package ca.concordia.cssdev.plugin.annotations;

public enum CSSAnnotationType {
	
	DUPLICATION("ca.concordia.cssdev.plugin.annotations.DuplicatedDeclarations"), 
	STYLE_RULE("ca.concordia.cssdev.plugin.annotations.StyleRule"),
	DECLARATION("ca.concordia.cssdev.plugin.annotations.Declaration"),
	AFFECTED_DOM_NODE("ca.concordia.cssdev.plugin.annotations.DomNode");
	
	private String value;
		
	private CSSAnnotationType(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}

}
