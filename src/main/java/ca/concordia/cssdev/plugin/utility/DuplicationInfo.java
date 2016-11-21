package ca.concordia.cssdev.plugin.utility;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.csshelper.CSSPropertyCategory;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class DuplicationInfo {
	
	private final ItemSet itemSet;
	private final IFile iFile;
	
	public DuplicationInfo(ItemSet itemSet, IFile iFile) {
		this.itemSet = itemSet;
		this.iFile = iFile;
	}

	public String getSelectorNames() {
		StringBuilder result = new StringBuilder();
		for (Iterator<Selector> iterator = itemSet.getSupport().iterator(); iterator.hasNext(); ) {
			result.append(iterator.next());
			if (iterator.hasNext())
				result.append(", ");
		}
		return result.toString();
	}

	public String getDeclarationNames() {
		StringBuilder result = new StringBuilder();
		for (Iterator<Item> iterator = itemSet.iterator(); iterator.hasNext(); ) {
			Item currentItem = iterator.next();
			if (currentItem.containsDifferencesInValues()) {
				result.append(currentItem.getDeclarationWithMinimumChars().getProperty());
			} else {
				result.append(currentItem.getDeclarationWithMinimumChars());
			}
			if (iterator.hasNext())
				result.append(", ");
		}
		return result.toString();
	}
	
	public double getRank() {
		return -1;
	}

	public IFile getSourceIFile() {
		return this.iFile;
	}

	public ItemSet getItemSet() {
		return this.itemSet;
	}

	public boolean hasDifferences() {
		return itemSet.containsDifferencesInValues();
	}

	public Set<Integer> getDuplicationTypes() {
		Set<Integer> duplicationTypes = new TreeSet<>(Comparator.naturalOrder());
		for (Item item : getItemSet()) {
			duplicationTypes.addAll(item.getDuplicationTypes());
		}
		return duplicationTypes;
	}

	public Set<CSSPropertyCategory> getPropertyCategories() {
		Set<CSSPropertyCategory> categories = new LinkedHashSet<>();
		
		for (Declaration declaration : getItemSet().getRepresentativeDeclarations()) {
			categories.add(declaration.getPropertyCategory());
		}
		
		return categories;
	}

	public void drawCategoryMarks(GC gc, int startX, int startY) {
		drawCategoryMarks(gc, startX, startY, getPropertyCategories().toArray(new CSSPropertyCategory[]{}));
	}
	
	public static void drawCategoryMarks(GC gc, int startX, int startY, CSSPropertyCategory... propertyCategories) {
		final int GAP_X = 2;
		final int WIDTH = 17;
		final Color CIRCLES_BORDER_COLOR = new Color(Display.getDefault(), new RGB(150, 150, 150));
		Color black = new Color(Display.getDefault(), new RGB(0,0,0));
		Color white = new Color(Display.getDefault(), new RGB(255,255,255));
		gc.setAntialias(SWT.ON);
		int x = startX + GAP_X;
		FontData[] fontData = gc.getFont().getFontData();
		fontData[0].setHeight(6);
		Font font = new Font(Display.getCurrent(), fontData[0]);
		for (CSSPropertyCategory category : propertyCategories) {
			String hex = category.getHexColor().substring(1);
			int r = Integer.parseInt(hex.substring(0, 2), 16);
			int g = Integer.parseInt(hex.substring(2, 4), 16);
			int b = Integer.parseInt(hex.substring(4, 6), 16);
			Color bgColor = new Color(Display.getDefault(), new RGB(r, g, b));
			gc.setBackground(bgColor);
			gc.fillOval(x, startY, WIDTH, WIDTH);
			gc.setFont(font);
			Color color = gc.getBackground(); 
			double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
			Color inverse = y >= 128 ? black : white;
			gc.setForeground(inverse);
			Point textExtent = gc.textExtent(category.getShortDescription());
			gc.drawText(category.getShortDescription(), x + (WIDTH - textExtent.x) / 2, startY + (WIDTH - textExtent.y) / 2);
			gc.setForeground(CIRCLES_BORDER_COLOR);
			gc.drawOval(x, startY, WIDTH - 1, WIDTH - 1);
			x += WIDTH + GAP_X;
			bgColor.dispose();
		}
		font.dispose();
		black.dispose();
		white.dispose();
	}
}
